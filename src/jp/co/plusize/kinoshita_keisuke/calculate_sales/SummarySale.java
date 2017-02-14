package jp.co.plusize.kinoshita_keisuke.calculate_sales;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
public class SummarySale {
	private static final String List = null;
	public static void main(String[] args) throws IOException {
		HashMap<String,String> branch = new HashMap<String,String>();
		HashMap<String,Long> branchSales = new HashMap<String,Long>();
		HashMap<String,String> commodity = new HashMap<String,String>();
		HashMap<String,Long> commoditySales = new HashMap<String,Long>();
		if(args.length != 1 ) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		}
		if(!inputFile(args[0] , "branch.lst" , "支店" , "[0-9]{3}" , branch , branchSales)) {
			return;
		}
		if(!inputFile(args[0] , "commodity.lst" , "商品" , "[A-Z|a-z|0-9]{8}" , commodity , commoditySales)) {
			return;
		}
		//集計
		File dir =new File(args[0]);
		File[] files = dir.listFiles();
		ArrayList<File> list = new ArrayList<File>();
		try {
			for(int i = 0; i < files.length; i++) {
				File file = files[i];
				String fileName = file.getName(); //ファイルからファイル名を取得
				if(fileName.matches("[0-9]{8}.rcd$") && file.isFile()){
					list.add(file);
				}
			}
			//連番チェック
			for(int i =0; i + 1 < list.size() - 1; i++) {
				String str1 = list.get(i).getName().substring(0, 8);
				String str2 = list.get(i+1).getName().substring(0, 8);
				int one = Integer.parseInt(str1);
				int two = Integer.parseInt(str2);

				if(two - one != 1){
					System.out.println("売上ファイル名が連番になっていません");
					return;
				}
			}

		} catch(Exception e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		}
		if(!aggregate(args[0] , code1 , 0 , branch , "支店" , sum1 , branchSales )) {
			return;
		}
		if(!aggregate(args[0] , code2 ,1 , commodity , "商品" , sum2 , commoditySales)) {
			return;
		}
		if(!outputFile(args[0] , "branch.out" , branchSales , branch)) {
			return;
		}
		if(!outputFile(args[0] , "commodity.out" , commoditySales , commodity)) {
			return;
		}
	}
		public static boolean outputFile(String path , String fileName , HashMap<String,Long> saleMap , HashMap<String,String>nameMap) {
		BufferedWriter bw =null;
		try {
			//支店別集計ファイルを出力
			File file = new File(path,fileName);
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			ArrayList<Map.Entry<String,Long>> entries = new ArrayList<Map.Entry<String, Long>>(saleMap.entrySet());
			Collections.sort(entries, new Comparator<Map.Entry<String,Long>>() {

				public int compare(Entry<String,Long> entry1,Entry<String,Long> entry2) {
					return((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
				}
			});
			for(Entry<String,Long> s : entries) {
				bw.write(s.getKey() + ","+nameMap .get(s.getKey()) + "," + s.getValue() + System.getProperty("line.separator"));
			}
		} catch(Exception e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;
		} finally {
			if(bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return false;
				}
			}

		}
		return true;
	}
	public static boolean aggregate(String path , String code , int n ,HashMap<String,String> nameMap , String item , Long sum ,  HashMap<String,Long> saleMap) {
		File dir =new File(path);
		File[] files = dir.listFiles();
		ArrayList<File> list = new ArrayList<File>();
		BufferedReader br = null;
		try {
			for(int i = 0; i < list.size(); i++) {
				ArrayList<String> proceeds = new ArrayList<String>();
				FileReader fr = new FileReader(list.get(i));
				br  = new BufferedReader(fr);
				String s;
				//proceedsに各値を加えてリストに入れる


				while((s = br.readLine()) != null) { //ファイルの中を参照
					String str = s;
					proceeds.add(str);
				}
				if(proceeds.size() != 3) {
					System.out.println(list.get(i).getName() + "のフォーマットが不正です");
					return false;
				}

				//マップに支店コードと売上金額を入れて保持
				code = proceeds.get(n);
				String name = proceeds.get(2); //売上額
				Long money = Long.parseLong(name);
				if(!nameMap.containsKey(code)) {
					System.out.println(list.get(i).getName() + "の" + item + "コードが不正です");
					return false;
				}

				//支店別集計
				sum = saleMap.get(code); //元の値を参照
				sum += money;
				if(sum > 9999999999L) {
					System.out.println("合計金額が10桁を超えました");
					return false;
				}
				saleMap.put(name,sum);

			}
		} catch(Exception e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;
		} finally {
			if(br != null) {
				try {
					br.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return false;
					}
				}
		}return true;
	}
	public static boolean inputFile(String path , String fileName , String item , String condition , HashMap<String,String> nameMap , HashMap<String,Long> saleMap) {
		BufferedReader br = null;
		try {
			//支店定義ファイルの読み込み
			File file = new File(path, fileName);
			if(!file.exists()) {
				System.out.println(item + "定義ファイルが存在しません");
				return false;
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String s;
			while((s = br.readLine()) != null) {
				String[] items = s.split(",");

				if(items.length != 2 || !items[0].matches(condition) || !file.isFile()) {
					System.out.println(item + "定義ファイルのフォーマットが不正です");
					return false;
				}
				nameMap.put(items[0],items[1]);
				saleMap.put(items[0],0L);
			}
		} catch(IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;
		} finally {
			if(br != null) {
				try {
					br.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return false;
				}
			}
		} return true;
	}
}
