package com.tmall.wireless.tac.biz.processor.firstScreenMind.utils;


public class RenderLangUtil {
	
	public static String safeString(Object val){
		if(val == null){
			return null;
		}
		if(val instanceof String){
			return (String) val;
		}else{
			return String.valueOf(val);
		}
	}
	
	public static Long safeLong(Object val){
		if(val == null){
			return null;
		}
		if(val instanceof Number){
			return ((Number) val).longValue();
		}
		if(val instanceof String){
			try{
				return Long.valueOf((String)val);
			}catch(Exception e){
				return null;
			}
		}
		return null;
	}
	
	public static Integer safeInteger(Object val){
		if(val == null){
			return null;
		}
		if(val instanceof Number){
			return ((Number) val).intValue();
		}
		if(val instanceof String){
			try{
				return Integer.valueOf((String)val);
			}catch(Exception e){
				return null;
			}
		}
		return null;
	}
	
	public static Boolean safeBoolean(Object obj){
		if(obj == null){
			return Boolean.FALSE;
		}
		if(obj instanceof String){
			return Boolean.valueOf((String) obj);
		}
		if(obj instanceof Boolean){
			return (Boolean) obj;
		}
		
		if(obj instanceof Number){
			return ((Number) obj).intValue() > 0;
		}
		return Boolean.FALSE;
	}
}

