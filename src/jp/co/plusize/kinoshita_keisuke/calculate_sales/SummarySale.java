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
	public static void main (String[] args) throws IOException {
		//支店コードと支店名
		HashMap<String,String> branchName = new HashMap<String,String>();
		//支店コードと売上額
		HashMap<String,Long> branchSales = new HashMap<String,Long>();
		//商品コードと商品名
		HashMap<String,String> commodityName = new HashMap<String,String>();
		//商品コードと売上額
		HashMap<String,Long> commoditySales = new HashMap<String,Long>();
		if (args.length != 1 ) {
			System.out.println ("予期せぬエラーが発生しました");
			return;
		}
		//支店定義ファイルの読み込み
		if (!inputFile(args[0] , "branch.lst" , "支店" , "[0-9]{3}" , branchName , branchSales)) {
			return;
		}
		//商品定義ファイルの読み込み
		if (!inputFile(args[0] , "commodity.lst" , "商品" , "[A-Z|a-z|0-9]{8}" , commodityName , commoditySales)) {
			return;
		}
		File dir = new File(args[0]);
		File[] files = dir.listFiles();
		ArrayList<File> rcdList = new ArrayList<File>();
		try {
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				String fileName = file.getName();
				if (fileName.matches("[0-9]{8}.rcd$") && file.isFile()) {
					rcdList.add (file);
				}
			}
			//連番チェック
			for (int i =0; i < rcdList.size() -1 ; i++) {
				String str1 = rcdList.get(i).getName().substring(0, 8);
				String str2 = rcdList.get(i+1).getName().substring(0, 8);
				int one = Integer.parseInt(str1);
				int two = Integer.parseInt(str2);

				if (two - one != 1 ) {
					System.out.println ("売上ファイル名が連番になっていません");
					return;
				}
			}

		} catch (Exception e) {
			System.out.println ("予期せぬエラーが発生しました");
			return;
		}
		//集計
		BufferedReader br = null;
		try {
			for (int i = 0; i < rcdList.size(); i++) {
				ArrayList<String> proceeds = new ArrayList<String>();
				FileReader fr = new FileReader (rcdList.get(i));
				br = new BufferedReader(fr);
				String s;

				while ((s = br.readLine()) != null) {
					String str = s;
					proceeds.add (str);
				}
				if (proceeds.size() != 3) {
					System.out.println (rcdList.get(i).getName() + "のフォーマットが不正です");
					return;
				}

				 //支店コード
				String branchCode = proceeds.get(0);
				 //商品コード
				String commodityCode = proceeds.get(1);
				//売上額
				String price = proceeds.get(2);
				Long money = Long.parseLong(price);
				if (!branchName.containsKey(branchCode)) {
					System.out.println (rcdList.get(i).getName() + "の支店コードが不正です");
					return;
				}
				if (!commodityName.containsKey(commodityCode)) {
					System.out.println (rcdList.get(i).getName() + "の商品コードが不正です");
					return;
				}
				//支店別集計
				long branchSum = branchSales.get(branchCode) + money;
				if (branchSum  > 9999999999L) {
					 System.out.println ("合計金額が10桁を超えました");
					 return;
				}
				branchSales.put(branchCode,branchSum );
				//商品別集計
				long commoditySum =commoditySales.get(commodityCode) + money;
				if (commoditySum > 9999999999L) {
					System.out.println ("合計金額が10桁を超えました");
					return;
				}
				commoditySales.put(commodityCode, commoditySum);

			}
		} catch (Exception e) {
			System.out.println ("予期せぬエラーが発生しました");
			return;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					System.out.println ("予期せぬエラーが発生しました");
					return;
				}
			}
		}
		//支店別集計結果の出力
		if (!outputFile(args[0] , "branch.out" , branchSales , branchName)) {
			return;
		}
		//商品別集計結果の出力
		if (!outputFile(args[0] , "commodity.out" , commoditySales , commodityName)) {
			return;
		}
	}
		public static boolean outputFile(String path , String fileName , HashMap<String,Long> saleMap , HashMap<String,String>nameMap) {
		BufferedWriter bw =null;
		try {
			File file = new File(path,fileName);
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			ArrayList<Map.Entry<String,Long>> entries = new ArrayList<Map.Entry<String, Long>>(saleMap.entrySet());
			Collections.sort(entries, new Comparator<Map.Entry<String,Long>>() {

				public int compare(Entry<String,Long> entry1,Entry<String,Long> entry2) {
					return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
				}
			});
			for (Entry<String,Long> s : entries) {
				bw.write(s.getKey() + ","+nameMap .get(s.getKey()) + "," + s.getValue() + System.getProperty("line.separator"));
			}
		} catch (Exception e) {
			System.out.println ("予期せぬエラーが発生しました");
			return false;
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
				System.out.println ("予期せぬエラーが発生しました");
				return false;
				}
			}

		}
		return true;
	}
	public static boolean inputFile(String path , String fileName , String item , String condition , HashMap<String,String> nameMap , HashMap<String,Long> saleMap) {
		BufferedReader br = null;
		try {
			File file = new File(path, fileName);
			if (!file.exists()) {
				System.out.println (item + "定義ファイルが存在しません");
				return false;
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String s;
			while ((s = br.readLine()) != null) {
				String[] items = s.split(",");

				if (items.length != 2 || !items[0].matches(condition) || !file.isFile()) {
					System.out.println (item + "定義ファイルのフォーマットが不正です");
					return false;
				}
				nameMap.put(items[0],items[1]);
				saleMap.put(items[0],0L);
			}
		} catch (IOException e) {
			System.out.println ("予期せぬエラーが発生しました");
			return false;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					System.out.println ("予期せぬエラーが発生しました");
					return false;
				}
			}
		} return true;
	}
}
