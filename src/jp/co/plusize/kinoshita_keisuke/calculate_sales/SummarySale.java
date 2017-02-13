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
		BufferedReader br = null;
		try {
			//支店定義ファイルの読み込み
			File file = new File(args[0], "branch.lst");
			if(!file.exists()) {
				System.out.println("支店定義ファイルが存在しません");
				return;
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String s;
			while((s = br.readLine()) != null) {
				String[] items = s.split(",");

				if(items.length != 2 || !items[0].matches("[0-9]{3}") || !file.isFile()) {
					System.out.println("支店定義ファイルのフォーマットが不正です");
					return;
				}
				branch.put(items[0],items[1]); //支店コードと支店名
				branchSales.put(items[0],0L); //支店コードと金額
			}
		} catch(IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			if(br != null) {
			br.close();
			}
		}
			//商品定義ファイルの読み込み
		try {
			File file = new File(args[0], "commodity.lst");
			if(!file.exists()) {
				System.out.println("商品定義ファイルが存在しません");
				return;
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String s;

			while((s = br.readLine()) != null) {
				String[] items = s.split(",");
				if(items.length != 2 || !items[0].matches("[A-Z|a-z|0-9]{8}") || !file.isFile()) {
					System.out.println("商品定義ファイルのフォーマットが不正です");
					return;

				}
				commodity.put(items[0],items[1]); //商品コードと商品名
				commoditySales.put(items[0], 0L); //商品コードと金額
			}
		} catch(IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			if(br != null) {
			br.close();
			}
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
		try {
			for(int i = 0; i < list.size(); i++) {
				ArrayList<String> proceeds = new ArrayList<String>();
				FileReader fr = new FileReader(list.get(i));
				br = new BufferedReader(fr);
				String s;
				//proceedsに各値を加えてリストに入れる


				while((s = br.readLine()) != null) { //ファイルの中を参照
					String str = s;
					proceeds.add(str);
				}
				if(proceeds.size() != 3) {
					System.out.println(list.get(i).getName() + "のフォーマットが不正です");
					return;
				}

				//マップに支店コードと売上金額を入れて保持
				String Code1 = proceeds.get(0);  //支店コード
				String Code2 = proceeds.get(1); //商品コード
				String name = proceeds.get(2); //売上額
				Long money = Long.parseLong(name);
				if(!branch.containsKey(Code1)) {
					System.out.println(list.get(i).getName() + "の支店コードが不正です");
					return;
				}
				if(!commodity.containsKey(Code2)) {
					System.out.println(list.get(i).getName() + "の商品コードが不正です");
					return;
				}
				//支店別集計
				long sum1 = branchSales.get(Code1); //元の値を参照
				sum1 += money;
				if(sum1 > 9999999999L) {
					 System.out.println("合計金額が10桁を超えました");
					 return;
				}
				branchSales.put(Code1,sum1);
				//商品別集計
				long sum2 =commoditySales.get(Code2);
				sum2 += money;
				if(sum2 > 9999999999L) {
					System.out.println("合計金額が10桁を超えました");
					return;
				}
				commoditySales.put(Code2, sum2);

			}
		} catch(Exception e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			if(br != null) {
			br.close();
			}
		}
		BufferedWriter bw =null;
		try {
			//支店別集計ファイルを出力
			File file = new File(args[0],"branch.out");
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			ArrayList<Map.Entry<String,Long>> entries = new ArrayList<Map.Entry<String, Long>>(branchSales.entrySet());
			Collections.sort(entries, new Comparator<Map.Entry<String,Long>>() {

				public int compare(Entry<String,Long> entry1,Entry<String,Long> entry2) {
					return((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
				}
			});
			for(Entry<String,Long> s : entries) {
				bw.write(s.getKey() + ","+ branch.get(s.getKey()) + "," + s.getValue() + System.getProperty("line.separator"));
			}
		} catch(Exception e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			if(bw != null) {
			bw.close();
			}
		}
		try {
			//商品別集計ファイルに出力
			File file = new File(args[0],"commodity.out");
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			ArrayList<Map.Entry<String,Long>> entries = new ArrayList<Map.Entry<String, Long>>(commoditySales.entrySet());
			Collections.sort(entries, new Comparator<Map.Entry<String,Long>>() {

				public int compare(Entry<String,Long> entry1,Entry<String,Long> entry2) {
					return((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
				}
			});
			for(Entry<String,Long> s : entries) {
				bw.write(s.getKey() + ","+ commodity.get(s.getKey()) + ","+s.getValue() + System.getProperty("line.separator"));
			}
		} catch(Exception e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			if(bw != null) {
			bw.close();
			}
		}
	}
	public static void output(String n , String m) {
		BufferedWriter bw =null;
		try {
			//支店別集計ファイルを出力
			File file = new File(args[0],"n.out");
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			ArrayList<Map.Entry<String,Long>> entries = new ArrayList<Map.Entry<String, Long>>(m.entrySet());
			Collections.sort(entries, new Comparator<Map.Entry<String,Long>>() {

				public int compare(Entry<String,Long> entry1,Entry<String,Long> entry2) {
					return((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
				}
			});
			for(Entry<String,Long> s : entries) {
				bw.write(s.getKey() + ","+ n.get(s.getKey()) + "," + s.getValue() + System.getProperty("line.separator"));
			}
		} catch(Exception e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			if(bw != null) {
			bw.close();
			}
		}
	}
}