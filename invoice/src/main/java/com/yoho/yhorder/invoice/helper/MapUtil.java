package com.yoho.yhorder.invoice.helper;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.MapUtils;

import java.util.*;

/**
 * map的工具类
 * @author MALI
 * @author chenchao
 */
public final class MapUtil {
	/**
	 * Iterator 转换成map
	 * @param values
	 * @param keyFunction
	 * @return
	 */
	public static <K, V> Map<K, V> transformMap(Iterable<V> values, Function<? super V, K> keyFunction) {
		if (Iterables.isEmpty(values) || null == keyFunction) {
			return Maps.newHashMap();
		}
		
		Map<K, V> builder = new HashMap<K, V>();
		Iterator<V> iterator = values.iterator();
	    while (iterator.hasNext()) {
	      V value = iterator.next();
	      builder.put(keyFunction.apply(value), value);
	    }
		return builder;
	}
	
	/**
	 *  Iterator 转换成<key,List>
	 * @param values 
	 * @param keyFunction
	 * @return
	 */
	public static <V, T, K> Map<T, K> transformListMap(Iterable<V> values, FunctionExPlus<V, T, K> keyFunction) {
		if (Iterables.isEmpty(values) || null == keyFunction) {
			return Maps.newHashMap();
		}
		Map<T, K> builder = new HashMap<T, K>();
		Iterator<V> iterator = values.iterator();
		while (iterator.hasNext()) 
		{
			V value = iterator.next();
			builder.put(keyFunction.applyKey(value), keyFunction.applyValue(value));
		}
		return builder;
	}
	
	/**
	 *  Iterator 转换成<key,List>
	 * @param values 
	 * @param keyFunction
	 * @return
	 */
	public static <K, V> Map<K, List<V>> transformListMap(Iterable<V> values, Function<? super V, K> keyFunction) {
		if (Iterables.isEmpty(values) || null == keyFunction) {
			return Maps.newHashMap();
		}
		Map<K, List<V>> builder = new HashMap<K, List<V>>();
		Iterator<V> iterator = values.iterator();
	    while (iterator.hasNext()) 
	    {
	      V value = iterator.next();
	      if(builder.containsKey(keyFunction.apply(value)))
	      {
	    	  builder.get(keyFunction.apply(value)).add(value);
	      }else
	      {	
	    	  List<V> newArrayList=new ArrayList<V>();
	    	  newArrayList.add(value);
	    	  builder.put(keyFunction.apply(value), newArrayList);
	      }
	    }
		return builder;
	}
	
	public static <T> List<T> getList(Map<Integer, List<T>> map)
	{
		if(MapUtils.isEmpty(map))
		{
			return Lists.newArrayList();
		}
		List<T> newArrayList = Lists.newArrayList();
		for (List<T> item : map.values()) {
			newArrayList.addAll(item);
		}
		return newArrayList;
	}
	
	public interface Function<F, T> {
		T apply(F input);
	}
	
	public interface FunctionExPlus<F, T, K> {
		T applyKey(F input);
		K applyValue(F input);
	}
}