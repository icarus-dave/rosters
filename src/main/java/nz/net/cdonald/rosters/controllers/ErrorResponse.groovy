package nz.net.cdonald.rosters.controllers

public class ErrorResponse {
	int code
	String message

	public ErrorResponse(int code, String message) {
		this.code = code
		this.message = message
	}
}
