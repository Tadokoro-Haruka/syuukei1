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

		HashMap<String, String> branchNamemap = new HashMap<String,String>();
		HashMap<String, Long> branchSalemap = new HashMap<String,Long>();

		HashMap<String, String>commodityNamemap = new HashMap<String,String>();
		HashMap<String, Long>commoditySalemap = new HashMap<String,Long>();

		if(!fileRead(args[0],"branch.lst",branchNamemap,branchSalemap,"[0-9]{3}","支店")){
			return;
		}
		if(!fileRead(args[0],"commodity.lst",commodityNamemap,commoditySalemap,"[A-Z0-9]{8}","商品")){
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

		for (int i = 0; i<list.size() - 1 ; i++){
			String s =  files[i].getName();
			String ss = s.substring(0,8);

			int j = Integer.parseInt(ss);

			String s1 =  files[i+1].getName();
			String ss1 = s1.substring(0,8);

			int k = Integer.parseInt(ss1);

			if ( k - j != 1){
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}

		for (int i = 0; i< list.size(); i++){
			BufferedReader bffr = null;
			try{
				ArrayList<String> rcdRead = new ArrayList<String>();
				FileReader fr = new FileReader(list.get(i));
				bffr = new BufferedReader(fr);

				String s;
				while((s = bffr.readLine())!= null){
					rcdRead.add(s);
				}

				if(rcdRead.size() != 3){
					System.out.println(list.get(i).getName() + "のフォーマットが不正です");
					return;
				}

				String branchCode = rcdRead.get(0);

				String commodityCode = rcdRead.get(1);

				if(!branchSalemap.containsKey(branchCode)){
					System.out.println(list.get(i).getName() + "の支店コードが不正です");
					return;
				}

				if(!commoditySalemap.containsKey(commodityCode)){
					System.out.println(list.get(i).getName() + "の商品コードが不正です");
					return;
				}

				long g = Long.parseLong(rcdRead.get(2));
				Long test = branchSalemap.get(branchCode) + g;
				if(test > 1000000000){
					System.out.println("合計金額が10桁を超えました");
					return;
				}

				branchSalemap.put(branchCode,test);

				long n = Long.parseLong(rcdRead.get(2));
				Long tesst = commoditySalemap.get(commodityCode) + n;
				commoditySalemap.put(commodityCode,tesst);

			}catch(FileNotFoundException e){
			}catch(IOException e){
			}finally{
				try{
					if(bffr != null);
					bffr.close();
				}catch(Exception e){
					System.out.println("予期せぬエラーが発生しました");
					return;
				}
			}
		}

		if(!fileout(args[0],"branch.out",branchNamemap,branchSalemap)){
			return;
		}

		if(!fileout(args[0],"commodity.out",commodityNamemap,commoditySalemap)){
			return;
		}
	}


	public static boolean fileRead(String dirPath,String fileName,HashMap<String, String> names,
		HashMap<String, Long> sales,String code,String message){
		BufferedReader br = null;

		try{
			File file = new File(dirPath,fileName);
			if(!file.exists()){
				System.out.println(message + "定義ファイルが存在しません");
				return false;
			}

			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String s;
			while((s = br.readLine())!= null){
				String[] arrey = s.split(",");
				String i = arrey[0];
				if(!i.matches(code) || (arrey.length !=2)){
					System.out.println(message + "定義ファイルのフォーマットが不正です");
					return false;
				}
				names.put(arrey[0],arrey[1]);
				sales.put(arrey[0],0L);
			}
		}catch(Exception e){
			return false;
		}finally{
			try{
				if(br != null);
				br.close();
			}catch(Exception e){
				System.out.println("予期せぬエラーが発生しました");
				return false;
			}
		}
			return true;
		}



	public static boolean fileout(String dirPath,String fileName,HashMap<String,String> names,HashMap<String,Long> sales){

		File filey = new File(dirPath,fileName);
		BufferedWriter bw = null;

		try{
			FileWriter fileywriter = new FileWriter(filey);
			bw = new BufferedWriter(fileywriter);
			List<Map.Entry<String,Long>> commodityentries =
					new ArrayList<Map.Entry<String,Long>>(sales.entrySet());
			Collections.sort(commodityentries, new Comparator<Map.Entry<String,Long>>() {
				public int compare(
					Entry<String,Long> entry1, Entry<String,Long> entry2) {
					return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
					}
			});

			for (Entry<String,Long> entryy : commodityentries){
				bw.write(entryy.getKey()+","+ names.get(entryy.getKey())+","+entryy.getValue());
				bw.newLine();
			}
		}catch(IOException e){
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
