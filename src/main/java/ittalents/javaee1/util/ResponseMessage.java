package ittalents.javaee1.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Setter
@Getter
@AllArgsConstructor
public class ResponseMessage {
	private String msg;
	private int status;
	private LocalDateTime timeCause;
}
