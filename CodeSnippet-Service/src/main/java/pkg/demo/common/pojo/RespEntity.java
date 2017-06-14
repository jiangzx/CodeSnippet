package pkg.demo.common.pojo;

import lombok.Data;

@Data
public class RespEntity<T> {
	private T results;
	private Integer code;
	
	public RespEntity(T data,Integer code){
		this.results = data;
		this.code = code;
	}
}
