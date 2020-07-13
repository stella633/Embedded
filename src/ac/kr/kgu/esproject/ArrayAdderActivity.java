package ac.kr.kgu.esproject;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Random;

public class ArrayAdderActivity extends Activity implements Button.OnClickListener{
	/** Called when the activity is first created. */

	private static final float FONT_SIZE = 18;
	private Spinner s;
	private Button pushButton;
	private Button ConfirmButton;
	private Button clearButton;
	private EditText answer; 
	private TextView setText;
	private Random random = new Random();
	private LinearLayout dynamicLayout;
	private LinearLayout sub;
	int[] values; 
	int result;
	int flag,flag2;//flag은 정답체크/ falg2는 확인버튼 잠금 
	Truebuzzer truebuzzer = new Truebuzzer();
	Falsebuzzer falsebuzzer = new Falsebuzzer();
	WaitSegment waitsegment = new WaitSegment();
	TrueSegment truesegment = new TrueSegment();
	FalseSegment falsesegment = new FalseSegment();
	WaitDotmatrix waitdotmatrix = new WaitDotmatrix();
	TrueDotmatrix truedotmatrix = new TrueDotmatrix();
	FalseDotmatrix falsedotmatrix = new FalseDotmatrix();
	Thread tb = new Thread(truebuzzer,"1");
	Thread fb = new Thread(falsebuzzer,"2");
	Thread ws = new Thread(waitsegment,"3");
	Thread ts = new Thread(truesegment,"4");
	Thread fs = new Thread(falsesegment,"5");
	Thread wd = new Thread(waitdotmatrix,"6");
	Thread td = new Thread(truedotmatrix,"7");
	Thread fd = new Thread(falsedotmatrix,"8");

	int tstop, fstop;
	int wstop, sstop;//7세그먼트 lock
	int wdtop, sdtop;//dotmatrix lock

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		System.loadLibrary("sum");
		System.loadLibrary("buzzers");
		System.loadLibrary("7segments");
		System.loadLibrary("dotmatrixs");


		//init
		result=0;
		tstop=1;
		fstop=1;
		wstop=0;
		sstop=1;
		sdtop=1;
		wdtop=0;

		tb = new Thread(truebuzzer,"1");
		tb.setDaemon(true);
		tb.start();
		fb = new Thread(falsebuzzer,"2");
		fb.setDaemon(true);
		fb.start();
		ws = new Thread(waitsegment,"3");
		ws.setDaemon(true);
		ws.start();
		ts = new Thread(truesegment,"4");
		ts.setDaemon(true);
		ts.start();
		fs = new Thread(falsesegment,"5");
		fs.setDaemon(true);
		fs.start();
		wd = new Thread(waitdotmatrix,"6");
		wd.setDaemon(true);
		wd.start();
		td = new Thread(truedotmatrix,"7");
		td.setDaemon(true);
		td.start();
		fd = new Thread(falsedotmatrix,"8");
		fd.setDaemon(true);
		fd.start();

		buzzerFromJNI(0);
		s = (Spinner)findViewById(R.id.Spinner);
		pushButton = (Button)findViewById(R.id.pushButton);
		ConfirmButton = (Button)findViewById(R.id.Confirm);
		clearButton = (Button)findViewById(R.id.Clear);
		pushButton.setOnClickListener(this);
		ConfirmButton.setOnClickListener(this);
		clearButton.setOnClickListener(this);
		answer = (EditText)findViewById(R.id.answer);
		setText = (TextView)findViewById(R.id.setText);

		dynamicLayout = (LinearLayout)findViewById(R.id.dynamicArea);
		sub = (LinearLayout)findViewById(R.id.child);
		sub.setVisibility(View.INVISIBLE);
		setText.setVisibility(View.INVISIBLE);
	}
	public void onClick(View v) {	
		int id = v.getId();

		if ( id == R.id.pushButton ) {
			pushButton();
		}else if( id == R.id.Confirm)
		{
			try {
				ConfirmButton();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if( id == R.id.Clear)
		{
			try {
				clearButton();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	private void pushButton() {
		int num = Integer.parseInt(s.getSelectedItem().toString());
		int[] nums = new int[num];
		values = new int[num];
		TextView[] view = new TextView[num];

		dynamicLayout.removeAllViews();//다이나믹 레이아웃 초기화
		sub.setVisibility(View.VISIBLE);//결과 및 버튼 보이기
		result=0;
		//스피너 개수만큼 배열 생성
		for(int i=0;i<num;i++)
		{
			nums[i]=random.nextInt(10);
			values[i]=nums[i];
			result+=values[i];
			view[i] = new TextView(this);
			view[i].setId(nums[i]);
			view[i].setText(" 배열 요소#" + i +" : "+ nums[i]);
			view[i].setTextSize(FONT_SIZE);
			view[i].setTextColor(Color.WHITE);
			dynamicLayout.addView(view[i], new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		}


		
	}
	private void ConfirmButton()throws InterruptedException {
		if(flag2==1)
			return;
		Thread.sleep(200);
		if(answer.getText().toString().getBytes().length<=0)
		{
			setText.setText("값을 입력해주세요.");
			fstop=0;//false쓰레드 시작
			setText.setVisibility(View.VISIBLE);
			flag=0;
			return;
		}
		for(int i=0;i<answer.getText().length();i++){
			char c = answer.getText().charAt(i);
			if(c<48 || c> 57){//숫자가 아닌 경우
				{
					setText.setText("숫자만 입력해주세요.");
					fstop=0;//false쓰레드 시작
					setText.setVisibility(View.VISIBLE);
					flag=0;
					return;
				}
			}
		}
		if(Integer.parseInt(answer.getText().toString())>1000000)
		{
			setText.setText("100만까지 입력 가능합니다.");
			fstop=0;//false쓰레드 시작
			setText.setVisibility(View.VISIBLE);
			flag=0;
			return;
		}



		flag = sumFromJNI(values,Integer.parseInt(answer.getText().toString()));

		wstop=1;//대기 세그먼트 잠금
		wdtop=1;
		if(flag ==1 ){
			setText.setText("정확합니다.");
			Thread.sleep(200);
			tstop=0;//true쓰레드 시작

		}else 
		{
			setText.setText("틀렸습니다.");
			Thread.sleep(200);
			fstop=0;//false쓰레드 시작

		}
		flag2=1;//확인버튼 잠금
		sstop=0;//세그먼트출력 시작
		sdtop=0;;
		setText.setVisibility(View.VISIBLE);
	}

	private void clearButton() throws InterruptedException {
		dynamicLayout.removeAllViews();//다이나믹 레이아웃 초기화
		sub.setVisibility(View.INVISIBLE);
		setText.setVisibility(View.INVISIBLE);
		flag2=0;//확인버튼 잠금해제
		result=0;
		sstop=1;
		sdtop=1;
		if(flag==1)
		{
			tstop=1;//true쓰레드 잠금
		}
		else
		{
			fstop=1;//false쓰레드 잠금
		}
		Thread.sleep(200);
		buzzerFromJNI(0);
		wstop=0;//대기세그먼트 시작
		//wdtop=0;
	}


	class Truebuzzer implements Runnable{



		public void run() {
			// TODO Auto-generated method stub

			while(true){
				if(tstop==0){
					try {
						buzzerFromJNI(1);
						Thread.sleep(300);
						buzzerFromJNI(0);
						Thread.sleep(100);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		}
	}
	class Falsebuzzer implements Runnable{


		public void run() {
			// TODO Auto-generated method stub

			while(true){
				if(fstop==0)
				{
					buzzerFromJNI(1);
				}
			}
		}
	}

	class WaitSegment implements Runnable{


		public void run() {
			// TODO Auto-generated method stub
			String[] wait = {"aggggg","1ggggg","cggggg","gcgggg","ggcggg",
					"gggcgg","ggggcg","gggggc","gggggb","ggggga"
					,"ggggag","gggagg","ggaggg","gagggg"};

			while(true){
				for (int i = 0; i < 14 ; i++)
					for(int j=0; j < 4; j++){
						if(wstop==1)
							break;
						SegmentPrint(wait[i]);
					}


			}
		}
	}

	class TrueSegment implements Runnable{


		public void run() {
			// TODO Auto-generated method stub
			while(true){
				while(true){
					if(tstop==1||sstop==1)
						break;

					//출력 스트림 만들기
					char[] trueresult;
					char[] printresult = new char[6];
					String waitprint = "heeeef";
					int check=0,zero=0;
					int result = Integer.parseInt(answer.getText().toString());


					trueresult = new char[11];

					for(int i=0;i<11;i++)
					{
						trueresult[i]='g';
					}
					if(result==0)
					{
						trueresult[5]='0';
						zero=1;
					}

					if(result/100000 != 0 ) {
						if(zero==1)
							break;
						trueresult[0]=Character.forDigit (result/100000,10);
						check=1;
						result=result%100000;
					}

					if(result/10000 != 0 || check==1) {
						if(zero==1)
							break;
						trueresult[1]=Character.forDigit (result/10000,10);
						check=1;
						result=result%10000;
					}

					if(result/1000 != 0 || check==1) {
						if(zero==1)
							break;
						trueresult[2]=Character.forDigit (result/1000,10);
						check=1;
						result=result%1000;
					}

					if(result/100 != 0 || check==1) {
						if(zero==1)
							break;
						trueresult[3]=Character.forDigit (result/100,10);
						check=1;
						result=result%100;
					} 

					if(result/10 != 0 || check==1) {
						if(zero==1)
							break;
						trueresult[4]=Character.forDigit (result/10,10);
						check=1;
						result=result%10;
					}

					if(result != 0 || check==1) {
						if(zero==1)
							break;
						trueresult[5]=Character.forDigit (result,10);
						check=1;
					}

					if(tstop==1||sstop==1)
						break;
					//화면 출력
					for(int i = 0;i <=2;i++)
					{

						for(int j=0;j<10;j++)
						{	
							if(tstop==1 || sstop==1)
								break;
							SegmentPrint(waitprint);
						}
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}

					for(int i = 5;i >= 0;i--)
					{

						for(int j=5;j>=0;j--)
						{
							printresult[j]=trueresult[5+i-j];
						}
						for(int j=0;j<14;j++)
						{	
							if(tstop==1 ||sstop==1)
								break;
							SegmentPrint(new String(printresult));
						}

					}

					for(int i = 0;i <=2;i++)
					{

						for(int j=0;j<10;j++)
						{	
							if(tstop==1||sstop==1)
								break;
							SegmentPrint(waitprint);
						}
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					sstop=1;
					wstop=0;//대기세그먼트 시작
				}
			}
		}
	}
	class FalseSegment implements Runnable{


		public void run() {
			// TODO Auto-generated method stub

			while(true){
				while(true){
					if(fstop==1||sstop==1)
						break;
					char[] falseresult;
					char[] printresult = new char[6];
					String waitprint = "heeeef";
					int result1 = Integer.parseInt(answer.getText().toString());
					int result2 = result;
					int length1 = (int)(Math.log10(result1)+1);
					int length2 = (int)(Math.log10(result2)+1);
					int dummy;

					if(result1 == 0)
						length1 = 1;
					if(result2 == 0)
						length2 = 1;

					dummy = length1+length2+1;

					if(fstop==1 || sstop==1)
						break;
					//스트림 만들기
					if(dummy >6)
					{

						falseresult = new char[6+length1+length2];
						for(int i=0;i<falseresult.length;i++)
							falseresult[i]='g';

						for(int i=0;i<length1;i++)
						{

							if(result1==0)
							{
								falseresult[dummy] = '0';
								break;
							}
							falseresult[length1-i-1] = Character.forDigit (result1%10,10);
							result1 = result1/10;
						}
						falseresult[length1] = 'd';
						for(int i=0;i<length2;i++)
						{

							if(result2==0)
							{
								falseresult[dummy+length1+1] = '0';
								break;
							}
							falseresult[length1+length2-i] = Character.forDigit (result2%10,10);
							result2 = result2/10;
						}
					}
					else
					{
						if(fstop==1 || sstop==1)
							break;
						dummy=6-dummy;
						falseresult = new char[6+length1+length2+dummy];
						for(int i=0;i<falseresult.length;i++)
							falseresult[i]='g';

						for(int i=0;i<length1;i++)
						{

							if(result1==0)
							{
								falseresult[dummy] = '0';
								break;
							}
							falseresult[dummy+length1-i-1] = Character.forDigit (result1%10,10);
							result1 = result1/10;
						}
						falseresult[dummy+length1] = 'd';
						for(int i=0;i<length2;i++)
						{

							if(result2==0)
							{
								falseresult[dummy+length1+1] = '0';
								break;
							}
							falseresult[dummy+length1+length2-i] = Character.forDigit (result2%10,10);
							result2 = result2/10;
						}
					}
					//출력
					for(int i = 0;i <=2;i++)
					{

						for(int j=0;j<10;j++)
						{	
							if(fstop==1 || sstop==1)
								break;
							SegmentPrint(waitprint);
						}
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					for(int i = falseresult.length-6;i >= 0;i--)
					{

						if(fstop==1 || sstop==1)
							break;
						for(int j=0;j<6;j++)
						{
							printresult[5-j]=falseresult[i+j];
						}

						for(int j=0;j<14;j++)
						{	
							if(fstop==1 || sstop==1)
								break;
							SegmentPrint(new String(printresult));
						}

					}
					for(int i = 0;i <=2;i++)
					{


						for(int j=0;j<10;j++)
						{	
							if(fstop==1 || sstop==1)
								break;
							SegmentPrint(waitprint);
						}

						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					sstop=1;
					wstop=0;//대기세그먼트 시작

				}
			}
		}
	}

	class WaitDotmatrix implements Runnable{


		public void run() {

			int pfont[][] = {{ 0x70,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00 },
					{ 0x0f,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00 },
					{ 0x01,0x01,0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x00 },
					{ 0x00,0x00,0x00,0x01,0x01,0x01,0x01,0x00,0x00,0x00 },
					{ 0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x01,0x01,0x01 },
					{ 0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x0f},
					{ 0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x70 },
					{ 0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x40,0x40,0x40 },
					{ 0x00,0x00,0x00,0x40,0x40,0x40,0x40,0x00,0x00,0x00 },
					{ 0x40,0x40,0x40,0x00,0x00,0x00,0x00,0x00,0x00,0x00 }};
			String pprint[] = new String[10];
			for (int i = 0; i < 10; i++) {
				pprint[i] = "";
				for (int j = 0; j < 10; j++) {
					String str = new String();
					str = Integer.toHexString((pfont[i][j]));
					// 자릿수를 맞추기 위해 한 자릿수일 경우엔 "0" 추가
					if (str.length() < 2)
						pprint[i] += "0";

					// 출력될 문자열에 이어 붙임
					pprint[i] += str;
				}
			}
			while(true)
			{
				// print
				for (int i = 0; i < 10; i++) {
					for (int j = 0; j < 4; j++) {
						if(wdtop==1)
							break;
						DotMatrixPrint(pprint[i]);
					}
				}

			}
		}
	}

	class TrueDotmatrix implements Runnable{

		public void run() {
			// TODO Auto-generated method stub
			int pfont[] = { 0x7f, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x7f };
			String pprint = new String();

			pprint = "";
			for (int j = 0; j < 10; j++) {

				String str = new String();
				str = Integer.toHexString((pfont[j]));
				// 출력될 문자열에 이어 붙임
				pprint += str;
			}
			while(true)
			{
				while(true)
				{


					if(tstop==1||sdtop==1)
						break;
					String input = answer.getText().toString();
					String print = new String();
					int ch;
					char buf[] = new char[100];

					buf = input.toCharArray();
					print = "00000000000000000000";

					for (int i = 0; i < input.length(); i++) {


						// 입력받은 문자를 아스키 코드로 변환
						ch = Integer.valueOf(buf[i]);
						// 미리 선언 된 문자 배열과 인덱스를 맞춤
						ch -= 0x20;

						// copy

						for (int j = 0; j < 5; j++) {
							if(tstop==1||sdtop==1)
								break;
							String str = new String();

							// 해당 수를 16진수 문자열로 변환
							str = Integer.toHexString((font[ch][j]));

							// 자릿수를 맞추기 위해 한 자릿수일 경우엔 "0" 추가
							if (str.length() < 2)
								print += "0";

							// 출력될 문자열에 이어 붙임
							print += str;
						}
						// 문자 사이 구분을 위한 공백 추가
						print += "00";

					}
					// 출력할 문자열의 맨 뒤쪽에 여백 추가
					print += "00000000000000000000";

					// print
					for(int i= 0; i<3; i++){
						for (int j = 0; j < 10; j++) {
							if(tstop==1||sdtop==1)
								break;
							DotMatrixPrint(pprint);
						}
						try {
							if(tstop==1||sdtop==1)
								break;
							DotMatrixPrint("00000000000000000000");
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					// print
					for (int i = (print.length() - 18)/2-1; i >=0; i--) {
						// 속도 제어
						for (int j = 0; j < 10; j++) {
							// 스레드 제어
							if(tstop==1||sdtop==1)
								break;
							DotMatrixPrint(print.substring(2 * i,2 * i + 20));
						}
					}

					// print
					for(int i= 0; i<3; i++){
						for (int j = 0; j < 10; j++) {
							if(tstop==1||sdtop==1)
								break;
							DotMatrixPrint(pprint);
						}
						try {
							if(tstop==1||sdtop==1)
								break;
							DotMatrixPrint("00000000000000000000");
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					sdtop=1;
					wdtop=0;
				}
			}

		}

	}



	class FalseDotmatrix implements Runnable{


		public void run() {
			// TODO Auto-generated method stub
			int pfont[] = { 0x7f, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x7f };
			String pprint = new String();

			pprint = "";

			for (int j = 0; j < 10; j++) {

				String str = new String();
				str = Integer.toHexString((pfont[j]));
				// 출력될 문자열에 이어 붙임
				pprint += str;
			}

			while(true)
			{
				while(true)
				{

					if(fstop==1||sdtop==1)
						break;
					String input = answer.getText().toString();
					String sums = Integer.toString(result);
					String simbol = "-";
					String print = new String();
					int ch;
					char buf[] = new char[100];

					buf = input.toCharArray();
					print = "00000000000000000000";

					for (int i = 0; i < input.length(); i++) {

						// 입력받은 문자를 아스키 코드로 변환
						ch = Integer.valueOf(buf[i]);
						// 미리 선언 된 문자 배열과 인덱스를 맞춤
						ch -= 0x20;

						// copy

						for (int j = 0; j < 5; j++) {

							String str = new String();

							// 해당 수를 16진수 문자열로 변환
							str = Integer.toHexString((font[ch][j]));

							// 자릿수를 맞추기 위해 한 자릿수일 경우엔 "0" 추가
							if (str.length() < 2)
								print += "0";

							// 출력될 문자열에 이어 붙임
							print += str;
						}
						// 문자 사이 구분을 위한 공백 추가
						print += "00";

					}
					buf = simbol.toCharArray();
					for (int i = 0; i < simbol.length(); i++) {


						// 입력받은 문자를 아스키 코드로 변환
						ch = Integer.valueOf(buf[i]);
						// 미리 선언 된 문자 배열과 인덱스를 맞춤
						ch -= 0x20;

						// copy

						for (int j = 0; j < 5; j++) {

							String str = new String();

							// 해당 수를 16진수 문자열로 변환
							str = Integer.toHexString((font[ch][j]));
							if (str.length() < 2)
								print += "0";

							// 출력될 문자열에 이어 붙임
							print += str;
						}
						// 문자 사이 구분을 위한 공백 추가
						print += "00";

					}

					buf = sums.toCharArray();
					for (int i = 0; i < sums.length(); i++) {


						// 입력받은 문자를 아스키 코드로 변환
						ch = Integer.valueOf(buf[i]);
						// 미리 선언 된 문자 배열과 인덱스를 맞춤
						ch -= 0x20;

						// copy

						for (int j = 0; j < 5; j++) {

							String str = new String();

							// 해당 수를 16진수 문자열로 변환
							str = Integer.toHexString((font[ch][j]));

							// 자릿수를 맞추기 위해 한 자릿수일 경우엔 "0" 추가
							if (str.length() < 2)
								print += "0";

							// 출력될 문자열에 이어 붙임
							print += str;
						}
						// 문자 사이 구분을 위한 공백 추가
						print += "00";

					}
					// 출력할 문자열의 맨 뒤쪽에 여백 추가
					print += "00000000000000000000";

					// print
					for(int i= 0; i<3; i++){
						for (int j = 0; j < 10; j++) {
							if(fstop==1||sdtop==1)
								break;
							DotMatrixPrint(pprint);
						}
						try {
							if(fstop==1||sdtop==1)
								break;
							DotMatrixPrint("00000000000000000000");
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					// print
					for (int i = (print.length() - 18)/2-1; i >=0; i--) {
						// 속도 제어
						for (int j = 0; j < 10; j++) {
							// 스레드 제어
							if(fstop==1||sdtop==1)
								break;
							DotMatrixPrint(print.substring(2 * i,2 * i + 20));
						}
					}

					// print
					for(int i= 0; i<3; i++){
						for (int j = 0; j < 10; j++) {
							if(fstop==1||sdtop==1)
								break;
							DotMatrixPrint(pprint);
						}
						try {
							if(fstop==1||sdtop==1)
								break;
							DotMatrixPrint("00000000000000000000");
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					sdtop=1;
					wdtop=0;
				}
			}

		}



	}
	public native int sumFromJNI(int values[],int result);
	public native int buzzerFromJNI(int value);
	public native String SegmentPrint(String values);
	public native int DotMatrixPrint(String values);







	public int font[][] = { /* 5x7 ASCII character font */
			{ 0x00, 0x00, 0x00, 0x00, 0x00 }, /* 0x20 space */
			{ 0x00, 0x00, 0x4f, 0x00, 0x00 }, /* 0x21 ! */
			{ 0x00, 0x07, 0x00, 0x07, 0x00 }, /* 0x22 " */
			{ 0x14, 0x7f, 0x14, 0x7f, 0x14 }, /* 0x23 # */
			{ 0x24, 0x2a, 0x7f, 0x2a, 0x12 }, /* 0x24 $ */
			{ 0x23, 0x13, 0x08, 0x64, 0x62 }, /* 0x25 % */
			{ 0x36, 0x49, 0x55, 0x22, 0x50 }, /* 0x26 & */
			{ 0x00, 0x05, 0x03, 0x00, 0x00 }, /* 0x27 ' */
			{ 0x00, 0x1c, 0x22, 0x41, 0x00 }, /* 0x28 ( */
			{ 0x00, 0x41, 0x22, 0x1c, 0x00 }, /* 0x29 ) */
			{ 0x14, 0x08, 0x3e, 0x08, 0x14 }, /* 0x2a * */
			{ 0x08, 0x08, 0x3e, 0x08, 0x08 }, /* 0x2b + */
			{ 0x00, 0x50, 0x30, 0x00, 0x00 }, /* 0x2c , */
			{ 0x08, 0x08, 0x08, 0x08, 0x08 }, /* 0x2d - */
			{ 0x00, 0x60, 0x60, 0x00, 0x00 }, /* 0x2e . */
			{ 0x20, 0x10, 0x08, 0x04, 0x02 }, /* 0x2f / */
			{ 0x3e, 0x51, 0x49, 0x45, 0x3e }, /* 0x30 0 */
			{ 0x00, 0x42, 0x7f, 0x40, 0x00 }, /* 0x31 1 */
			{ 0x42, 0x61, 0x51, 0x49, 0x46 }, /* 0x32 2 */
			{ 0x21, 0x41, 0x45, 0x4b, 0x31 }, /* 0x33 3 */
			{ 0x18, 0x14, 0x12, 0x7f, 0x10 }, /* 0x34 4 */
			{ 0x27, 0x45, 0x45, 0x45, 0x39 }, /* 0x35 5 */
			{ 0x3c, 0x4a, 0x49, 0x49, 0x30 }, /* 0x36 6 */
			{ 0x01, 0x71, 0x09, 0x05, 0x03 }, /* 0x37 7 */
			{ 0x36, 0x49, 0x49, 0x49, 0x36 }, /* 0x38 8 */
			{ 0x06, 0x49, 0x49, 0x29, 0x1e }, /* 0x39 9 */
			{ 0x00, 0x36, 0x36, 0x00, 0x00 }, /* 0x3a : */
			{ 0x00, 0x56, 0x36, 0x00, 0x00 }, /* 0x3b ; */
			{ 0x08, 0x14, 0x22, 0x41, 0x00 }, /* 0x3c < */
			{ 0x14, 0x14, 0x14, 0x14, 0x14 }, /* 0x3d = */
			{ 0x00, 0x41, 0x22, 0x14, 0x08 }, /* 0x3e > */
			{ 0x02, 0x01, 0x51, 0x09, 0x06 }, /* 0x3f ? */
			{ 0x32, 0x49, 0x79, 0x41, 0x3e }, /* 0x40 @ */
			{ 0x7e, 0x11, 0x11, 0x11, 0x7e }, /* 0x41 A */
			{ 0x7f, 0x49, 0x49, 0x49, 0x36 }, /* 0x42 B */
			{ 0x3e, 0x41, 0x41, 0x41, 0x22 }, /* 0x43 C */
			{ 0x7f, 0x41, 0x41, 0x22, 0x1c }, /* 0x44 D */
			{ 0x7f, 0x49, 0x49, 0x49, 0x41 }, /* 0x45 E */
			{ 0x7f, 0x09, 0x09, 0x09, 0x01 }, /* 0x46 F */
			{ 0x3e, 0x41, 0x49, 0x49, 0x7a }, /* 0x47 G */
			{ 0x7f, 0x08, 0x08, 0x08, 0x7f }, /* 0x48 H */
			{ 0x00, 0x41, 0x7f, 0x41, 0x00 }, /* 0x49 I */
			{ 0x20, 0x40, 0x41, 0x3f, 0x01 }, /* 0x4a J */
			{ 0x7f, 0x08, 0x14, 0x22, 0x41 }, /* 0x4b K */
			{ 0x7f, 0x40, 0x40, 0x40, 0x40 }, /* 0x4c L */
			{ 0x7f, 0x02, 0x0c, 0x02, 0x7f }, /* 0x4d M */
			{ 0x7f, 0x04, 0x08, 0x10, 0x7f }, /* 0x4e N */
			{ 0x3e, 0x41, 0x41, 0x41, 0x3e }, /* 0x4f O */
			{ 0x7f, 0x09, 0x09, 0x09, 0x06 }, /* 0x50 P */
			{ 0x3e, 0x41, 0x51, 0x21, 0x5e }, /* 0x51 Q */
			{ 0x7f, 0x09, 0x19, 0x29, 0x46 }, /* 0x52 R */
			{ 0x26, 0x49, 0x49, 0x49, 0x32 }, /* 0x53 S */
			{ 0x01, 0x01, 0x7f, 0x01, 0x01 }, /* 0x54 T */
			{ 0x3f, 0x40, 0x40, 0x40, 0x3f }, /* 0x55 U */
			{ 0x1f, 0x20, 0x40, 0x20, 0x1f }, /* 0x56 V */
			{ 0x3f, 0x40, 0x38, 0x40, 0x3f }, /* 0x57 W */
			{ 0x63, 0x14, 0x08, 0x14, 0x63 }, /* 0x58 X */
			{ 0x07, 0x08, 0x70, 0x08, 0x07 }, /* 0x59 Y */
			{ 0x61, 0x51, 0x49, 0x45, 0x43 }, /* 0x5a Z */
			{ 0x00, 0x7f, 0x41, 0x41, 0x00 }, /* 0x5b [ */
			{ 0x02, 0x04, 0x08, 0x10, 0x20 }, /* 0x5c \ */
			{ 0x00, 0x41, 0x41, 0x7f, 0x00 }, /* 0x5d ] */
			{ 0x04, 0x02, 0x01, 0x02, 0x04 }, /* 0x5e ^ */
			{ 0x40, 0x40, 0x40, 0x40, 0x40 }, /* 0x5f _ */
			{ 0x00, 0x01, 0x02, 0x04, 0x00 }, /* 0x60 ` */
			{ 0x20, 0x54, 0x54, 0x54, 0x78 }, /* 0x61 a */
			{ 0x7f, 0x48, 0x44, 0x44, 0x38 }, /* 0x62 b */
			{ 0x38, 0x44, 0x44, 0x44, 0x20 }, /* 0x63 c */
			{ 0x38, 0x44, 0x44, 0x48, 0x7f }, /* 0x64 d */
			{ 0x38, 0x54, 0x54, 0x54, 0x18 }, /* 0x65 e */
			{ 0x08, 0x7e, 0x09, 0x01, 0x02 }, /* 0x66 f */
			{ 0x0c, 0x52, 0x52, 0x52, 0x3e }, /* 0x67 g */
			{ 0x7f, 0x08, 0x04, 0x04, 0x78 }, /* 0x68 h */
			{ 0x00, 0x04, 0x7d, 0x00, 0x00 }, /* 0x69 i */
			{ 0x20, 0x40, 0x44, 0x3d, 0x00 }, /* 0x6a j */
			{ 0x7f, 0x10, 0x28, 0x44, 0x00 }, /* 0x6b k */
			{ 0x00, 0x41, 0x7f, 0x40, 0x00 }, /* 0x6c l */
			{ 0x7c, 0x04, 0x18, 0x04, 0x7c }, /* 0x6d m */
			{ 0x7c, 0x08, 0x04, 0x04, 0x78 }, /* 0x6e n */
			{ 0x38, 0x44, 0x44, 0x44, 0x38 }, /* 0x6f o */
			{ 0x7c, 0x14, 0x14, 0x14, 0x08 }, /* 0x70 p */
			{ 0x08, 0x14, 0x14, 0x18, 0x7c }, /* 0x71 q */
			{ 0x7c, 0x08, 0x04, 0x04, 0x08 }, /* 0x72 r */
			{ 0x48, 0x54, 0x54, 0x54, 0x20 }, /* 0x73 s */
			{ 0x04, 0x3f, 0x44, 0x40, 0x20 }, /* 0x74 t */
			{ 0x3c, 0x40, 0x40, 0x20, 0x7c }, /* 0x75 u */
			{ 0x1c, 0x20, 0x40, 0x20, 0x1c }, /* 0x76 v */
			{ 0x3c, 0x40, 0x30, 0x40, 0x3c }, /* 0x77 w */
			{ 0x44, 0x28, 0x10, 0x28, 0x44 }, /* 0x78 x */
			{ 0x0c, 0x50, 0x50, 0x50, 0x3c }, /* 0x79 y */
			{ 0x44, 0x64, 0x54, 0x4c, 0x44 }, /* 0x7a z */
			{ 0x00, 0x08, 0x36, 0x41, 0x00 }, /* 0x7b { */
			{ 0x00, 0x00, 0x77, 0x00, 0x00 }, /* 0x7c | */
			{ 0x00, 0x41, 0x36, 0x08, 0x00 }, /* 0x7d } */
			{ 0x08, 0x04, 0x08, 0x10, 0x08 }};/* 0x7e ~ */

}
