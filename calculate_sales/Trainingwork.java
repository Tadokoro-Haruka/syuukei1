package jp.alhinc.tadokoro_haruka.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Trainingwork {
	public static void main(String[] args) throws IOException{

		if (args.length != 1){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		HashMap<String, String> branchNameMap = new HashMap<>();
		HashMap<String, Long> branchSaleMap = new HashMap<>();

		HashMap<String, String>commodityNameMap = new HashMap<>();
		HashMap<String, Long>commoditySaleMap = new HashMap<>();

		if(!fileRead(args[0], "branch.lst", branchNameMap, branchSaleMap, "[0-9]{3}", "支店")){
			return;
		}
		if(!fileRead(args[0], "commodity.lst", commodityNameMap, commoditySaleMap, "[a-zA-Z0-9]{8}", "商品")){
			return;
		}


		File dir = new File(args[0]);
		ArrayList<File> list = new ArrayList<File>();

		File[] files = dir.listFiles();
		for (int i = 0; i< files.length; i++){
			if (files[i].isFile() && (files[i].getName().matches("[0-9]{8}.rcd"))){
				list.add(files[i]);
			}
		}

		for (int i = 0; i < list.size() - 1 ; i++){

			int number = Integer.parseInt(list.get(i).getName().substring(0,8));
			int nextNumber = Integer.parseInt(list.get(i + 1).getName().substring(0,8));

			if (nextNumber - number != 1){
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}

		for (int i = 0; i < list.size(); i++){
			BufferedReader bffr = null;
			try{
				ArrayList<String> rcdRead = new ArrayList<String>();
				FileReader fr = new FileReader(list.get(i));
				bffr = new BufferedReader(fr);

				String s;
				while((s = bffr.readLine()) != null){
					rcdRead.add(s);
				}

				if(rcdRead.size() != 3){
					System.out.println(list.get(i).getName() + "のフォーマットが不正です");
					return;
				}

				if(!rcdRead.get(2).matches("[0-9]+$")){
					System.out.println("予期せぬエラーが発生しました");
					return;
				}

				String branchCode = rcdRead.get(0);

				String commodityCode = rcdRead.get(1);

				if(!branchSaleMap.containsKey(branchCode)){
					System.out.println(list.get(i).getName() + "の支店コードが不正です");
					return;
				}

				if(!commoditySaleMap.containsKey(commodityCode)){
					System.out.println(list.get(i).getName() + "の商品コードが不正です");
					return;
				}

				long branchSales = Long.parseLong(rcdRead.get(2));
				Long test = branchSaleMap.get(branchCode) + branchSales;

				if(test > 1000000000){
					System.out.println("合計金額が10桁を超えました");
					return;
				}

				branchSaleMap.put(branchCode,test);

				long commoditySales = Long.parseLong(rcdRead.get(2));
				Long tesst = commoditySaleMap.get(commodityCode) + commoditySales;

				if(tesst > 1000000000){
					System.out.println("合計金額が10桁を超えました");
					return;
				}

				commoditySaleMap.put(commodityCode, tesst);

			}catch(FileNotFoundException e){
				System.out.println("予期せぬエラーが発生しました");
				return;
			}catch(IOException e){
				System.out.println("予期せぬエラーが発生しました");
				return;
			}finally{
				try{
					if(bffr != null){
						bffr.close();
					}
				}catch(Exception e){
					System.out.println("予期せぬエラーが発生しました");
					return;
				}
			}
		}

		if(!fileOut(args[0], "branch.out", branchNameMap, branchSaleMap)){
			return;
		}

		if(!fileOut(args[0], "commodity.out", commodityNameMap, commoditySaleMap)){
			return;
		}
	}


	public static boolean fileRead(String dirPath, String fileName, HashMap<String, String> names,
		HashMap<String, Long> sales, String code, String message){
		BufferedReader br = null;

		try{
			File file = new File(dirPath, fileName);
			if(!file.exists()){
				System.out.println(message + "定義ファイルが存在しません");
				return false;
			}

			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String s;
			while((s = br.readLine()) != null){
				String[] arrey = s.split(",");
				String splitList = arrey[0];
				if((arrey.length != 2) || !splitList.matches(code)){
					System.out.println(message + "定義ファイルのフォーマットが不正です");
					return false;
				}
				names.put(arrey[0], arrey[1]);
				sales.put(arrey[0], 0L);
			}
		}catch(Exception e){
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}finally{
			try{
				if(br != null){
				br.close();
				}
			}catch(Exception e){
				System.out.println("予期せぬエラーが発生しました");
				return false;
			}
		}
		return true;
	}



	public static boolean fileOut(String dirPath, String fileName, HashMap<String, String> names,
			HashMap<String, Long> sales){

		File filey = new File(dirPath, fileName);
		BufferedWriter bw = null;

		try{
			FileWriter fileWriter = new FileWriter(filey);
			bw = new BufferedWriter(fileWriter);
			List<Map.Entry<String,Long>> commodityEntry =
					new ArrayList<Map.Entry<String,Long>>(sales.entrySet());
			Collections.sort(commodityEntry, new Comparator<Map.Entry<String,Long>>() {
				public int compare(
					Entry<String,Long> entry1, Entry<String,Long> entry2) {
					return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
					}
			});

			for (Entry<String,Long> entryy : commodityEntry){
				bw.write(entryy.getKey() + "," +  names.get(entryy.getKey()) + "," + entryy.getValue());
				bw.newLine();
			}
		}catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}finally{
			try{
				if(bw != null){
					bw.close();
				}
			}catch(Exception e){
				System.out.println("予期せぬエラーが発生しました");
				return false;
			}
		}
		return true;
	}
}
