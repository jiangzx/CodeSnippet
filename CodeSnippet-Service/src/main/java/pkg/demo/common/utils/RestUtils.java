package pkg.demo.common.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import pkg.demo.common.pojo.RespEntity;

public class RestUtils {

	public static <T> ResponseEntity<RespEntity<T>> build(T t, HttpStatus status) {
		RespEntity<T> result = new RespEntity<T>(t, status.value());
		return ResponseEntity.status(status).body(result);
	}

	public static <T> ResponseEntity<RespEntity<T>> ok(T t) {
		return ResponseEntity.ok(new RespEntity<T>(t, HttpStatus.OK.value()));
	}

	public static ResponseEntity<RespEntity<String>> ok() {
		return ResponseEntity.ok(new RespEntity<String>("", HttpStatus.OK.value()));
	}
}
