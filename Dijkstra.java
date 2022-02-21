package test1;

import java.util.HashMap;
import java.util.Iterator;
//import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class Dijkstra {
	private ConcurrentHashMap<String,CResult> l_dists=new ConcurrentHashMap<String,CResult>();
	private ConcurrentHashMap<String,CResult> l_temps=new ConcurrentHashMap<String,CResult>();
	private int MaxCost=10000;
	private ConcurrentHashMap<String,Node> l_nodes=new ConcurrentHashMap<String,Node>();
	
	public  Dijkstra() {
	}
	public class CResult{
		CResult(){l_dist= MaxCost;}
		private Node l_node;
		private int l_dist;
		public Node GetLastNode() {return l_node;}
		public void SetLastNode(Node lv_node) {l_node=lv_node;}
		public int GetDistanceFromSrc() {return l_dist;}
		public void SetDistanceFromSrc(int lv_dist) {l_dist=lv_dist;}
	}
	public Node AddNode(String lv_id) {
		Node lv_node=new Node();
		lv_node.setIdentifier(lv_id);
		l_nodes.put(lv_id, lv_node);
		return lv_node;
	}
	public Node GetNode(String lv_id) {
		if (l_nodes.containsKey(lv_id)) return l_nodes.get(lv_id);else return null;
	}
	public void InitVisited() {
		for(ConcurrentHashMap.Entry<String,Node> lv_item:l_nodes.entrySet()) {
			lv_item.getValue().setVisited(false);
		}
	}
	public class Node{
		/**
		 * �ڵ�ı�ʶ��
		 */
		private String identifier;
		/**
		 * �ýڵ��Ƿ񱻷��ʹ�
		 */
		private boolean visited = false;
		/**
		 * �ýڵ��������ڵ��ӳ���ϵ
		 */
		private ConcurrentHashMap<Node,Integer> mapping = new ConcurrentHashMap<Node,Integer>();
		
		
		public String getIdentifier() {
			return identifier;
		}
		public void setIdentifier(String identifier) {
			this.identifier = identifier;
		}
		public boolean isVisited() {
			return visited;
		}
		public void setVisited(boolean visited) {
			this.visited = visited;
		}
		public ConcurrentHashMap<Node, Integer> getMapping() {
			return mapping;
		}
	}
	
	//@SuppressWarnings("unchecked")
	public  boolean getOptimalPath(){
		boolean lv_ok=false;
		//1. set init dist
		if (l_nodes==null || l_nodes.size()==0) return lv_ok;
		for(ConcurrentHashMap.Entry<String,Node>lv_vitem:l_nodes.entrySet()) { // V collection
			ConcurrentHashMap<String,Node> lv_temp_nodes=new ConcurrentHashMap<String,Node>();
			//reachable set distance
			lv_temp_nodes.putAll(l_nodes);
			lv_temp_nodes.remove(lv_vitem.getKey());
			ConcurrentHashMap<Node,Integer> lv_mapping = lv_vitem.getValue().getMapping();
			Iterator<Entry<Node, Integer>> lv_iterator = lv_mapping.entrySet().iterator();
			while (lv_iterator.hasNext()) {//src
				Entry<Node, Integer> lv_entry = (Entry<Node, Integer>) lv_iterator.next();
				Node lv_next = lv_entry.getKey();
				if (lv_next==lv_vitem) continue;
				CResult lv_temp=new CResult();
				lv_temp.SetDistanceFromSrc(lv_entry.getValue());
				lv_temp.SetLastNode(lv_vitem.getValue());
				l_temps.put(lv_vitem.getKey()+"-"+lv_next.getIdentifier(),lv_temp);
				l_dists.put(lv_vitem.getKey()+"-"+lv_next.getIdentifier(),lv_temp);
				lv_temp_nodes.remove(lv_next.getIdentifier());
				System.out.println("1-find and set temp,key="+lv_vitem.getKey()+"-"+lv_next.getIdentifier()+" dist="+String.valueOf(lv_temp.GetDistanceFromSrc()));
			}	
			//unreachable set max
			for(ConcurrentHashMap.Entry<String,Node>lv_item_temp:lv_temp_nodes.entrySet()) {
				CResult lv_temp=new CResult();
				lv_temp.SetDistanceFromSrc(MaxCost);
				lv_temp.SetLastNode(null);
				l_temps.put(lv_vitem.getKey()+"-"+lv_item_temp.getValue().getIdentifier(),lv_temp);
				l_dists.put(lv_vitem.getKey()+"-"+lv_item_temp.getValue().getIdentifier(),lv_temp);
				System.out.println("1-no find and set temp,key="+lv_vitem.getKey()+"-"+lv_item_temp.getValue().getIdentifier()+" dist="+String.valueOf(MaxCost));
			}
		}
		//computing node to node distance
		for(HashMap.Entry<String,Node>lv_vitem:l_nodes.entrySet()) { //V
			String lv_key=lv_vitem.getValue().getIdentifier();
			lv_ok=ShortestPath(lv_key);
			if (lv_ok) {
				System.out.println("-------------findpath="+lv_key+"->  table=---------");
				for(HashMap.Entry<String,CResult> lv_item:l_dists.entrySet()) {
					if(lv_item.getValue().GetLastNode()!=null)
						System.out.println("key="+lv_item.getKey()+" dist="+String.valueOf(lv_item.getValue().GetDistanceFromSrc())+" lastnode="+lv_item.getValue().GetLastNode().getIdentifier());
					else
						System.out.println("key="+lv_item.getKey()+" dist="+String.valueOf(lv_item.getValue().GetDistanceFromSrc())+" lastnode=null");
				}
			}
			else
				System.out.println("-------------findpath error---------");
		}
		return lv_ok;
	}
	
	
	//@SuppressWarnings("unchecked")
	private boolean ShortestPath(String lv_src_key) {
		ConcurrentHashMap<String,Node> lv_vnodes=new ConcurrentHashMap<String,Node>();
		Node lv_src;
		boolean lv_find=false;
		
		lv_vnodes.putAll(l_nodes);
		if (!l_nodes.containsKey(lv_src_key)) return lv_find;
		lv_src=lv_vnodes.remove(lv_src_key);
		System.out.println("2- add node to S(and remove from V) node key="+lv_src_key);
		//2.V-S<>0
		while (lv_vnodes.size()>0) {
			//3. find min dist
			Node lv_optimal_node=null;
			int lv_optimal_dist=MaxCost;
			// s->V
			for(ConcurrentHashMap.Entry<String,Node>lv_vitem:lv_vnodes.entrySet()) {
				String lv_key1=lv_src_key+"-"+lv_vitem.getKey();
				if(l_temps.containsKey(lv_key1)) {
					if (lv_optimal_dist>l_temps.get(lv_key1).GetDistanceFromSrc()) {
						lv_optimal_dist=l_temps.get(lv_key1).GetDistanceFromSrc();
						lv_optimal_node=lv_vitem.getValue();
					}
				}
			}
			if (lv_optimal_node!=null) {
				lv_vnodes.remove(lv_optimal_node.getIdentifier());
				System.out.println("2- add node to S(and remove from V) node key="+lv_optimal_node.getIdentifier());
				System.out.println("3-find min dist key="+lv_src_key+"-"+lv_optimal_node.getIdentifier()+" dist="+String.valueOf(lv_optimal_dist));
			}else {
				System.out.println("3-no find min dist, to break!");
				break;				
			}
			//4. for V-S update dist
			for(ConcurrentHashMap.Entry<String,Node>lv_vitem:lv_vnodes.entrySet()) {
				String lv_key2=lv_src.getIdentifier()+"-"+lv_optimal_node.getIdentifier();//s-j
				String lv_key3=lv_optimal_node.getIdentifier()+"-"+lv_vitem.getValue().getIdentifier();//j-i
				String lv_key4=lv_src.getIdentifier()+"-"+lv_vitem.getValue().getIdentifier();//s-i
				if(l_dists.get(lv_key2).GetDistanceFromSrc()+l_temps.get(lv_key3).GetDistanceFromSrc()<l_dists.get(lv_key4).GetDistanceFromSrc()) {
					l_dists.get(lv_key4).SetDistanceFromSrc(l_dists.get(lv_key2).GetDistanceFromSrc()+l_temps.get(lv_key3).GetDistanceFromSrc());
					l_dists.get(lv_key4).SetLastNode(lv_optimal_node);
					System.out.println("4- update dist,key="+lv_key4+" new dist="+String.valueOf(l_dists.get(lv_key4).GetDistanceFromSrc())+" last="+lv_optimal_node.getIdentifier());
				}
			}
			lv_find=true;
		}
		return lv_find;
	}
	
	//�㷨Ҫ�㣺
	//1. �����ڵ㼯�ϡ��ھӹ�ϵ�����룬�������������е�������
	//2. �����ڽӽڵ���루src-next)����������㵽�ڽӽڵ����(begin-next)��ѡ����̾����ڽ���Ϊ��һ������������������ʼ�㡢��һ����Ŀ��㡢��ʼ����һ�����롢��һ�ڵ�Ȳ���
	//3. ��ѭ���������ڽӾ��룩֮ǰ������ԴΪ�ѷ��ʣ����ýڵ㣩����������㵽Դ����̾������һ��
	//4. �ҵ��ڽӽڵ�==Ŀ��ڵ㣬�򷵻ؽ�����Ҳ�������null
	//5. ��������������ȫ��map����l_dists�У���Ϊ·�ɱ������ǵ��ƣ�,Ҫת��Ϊ����·�ɱ�
	//6. ��������ͼ�����������ֵҪ���αȽϣ�ȡСֵ����5-3��4-1�����ڴ����⣩
	//-------------result table---------
	//key=1-2 dist=7 lastnode=1
	//key=1-3 dist=9 lastnode=1
	//key=1-4 dist=20 lastnode=3
	//key=2-3 dist=10 lastnode=2
	//key=2-4 dist=15 lastnode=2
	//key=1-5 dist=20 lastnode=6
	//key=1-6 dist=11 lastnode=3
	//key=3-4 dist=11 lastnode=3
	//key=3-6 dist=2 lastnode=3
	//key=6-5 dist=9 lastnode=6
	
	
	
}

