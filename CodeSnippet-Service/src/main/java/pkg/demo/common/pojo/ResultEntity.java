package pkg.demo.common.pojo;

import lombok.Data;

@Data
public class ResultEntity<T> {
	private T results;
	private Integer code;
	
	public ResultEntity(T data,Integer code){
		this.results = data;
		this.code = code;
	}
}
