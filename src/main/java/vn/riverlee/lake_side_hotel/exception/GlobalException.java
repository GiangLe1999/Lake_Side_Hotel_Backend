package vn.riverlee.lake_side_hotel.exception;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import vn.riverlee.lake_side_hotel.dto.response.ErrorResponse;

import java.io.IOException;
import java.util.Date;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestControllerAdvice
@Slf4j
public class GlobalException {
    @ExceptionHandler({ConstraintViolationException.class,
            MissingServletRequestParameterException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(BAD_REQUEST)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Handle exception when the data invalid. (@RequestBody, @RequestParam, @PathVariable)",
                                    summary = "Handle Bad Request",
                                    value = """
                                            {
                                                 "timestamp": "2024-04-07T11:38:56.368+00:00",
                                                 "status": 400,
                                                 "path": "/api/v1/...",
                                                 "error": "Invalid Payload",
                                                 "message": "{data} must be not blank"
                                             }
                                            """
                            ))})
    })
    public ErrorResponse handleValidationException(Exception e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(new Date());
        errorResponse.setStatus(BAD_REQUEST.value());
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));

        String message = e.getMessage();
        if (e instanceof MethodArgumentNotValidException) {
            int start = message.lastIndexOf("[") + 1;
            int end = message.lastIndexOf("]") - 1;
            message = message.substring(start, end);
            errorResponse.setError("Invalid Payload");
            errorResponse.setMessage(message);
        } else if (e instanceof MissingServletRequestParameterException) {
            errorResponse.setError("Invalid Parameter");
            errorResponse.setMessage(message);
        } else if (e instanceof ConstraintViolationException) {
            errorResponse.setError("Invalid Parameter");
            errorResponse.setMessage(message.substring(message.indexOf(" ") + 1));
        } else {
            errorResponse.setError("Invalid Data");
            errorResponse.setMessage(message);
        }

        return errorResponse;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Bad Request",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "404 Response",
                                    summary = "Handle exception when resource not found",
                                    value = """
                                            {
                                              "timestamp": "2023-10-19T06:07:35.321+00:00",
                                              "status": 404,
                                              "path": "/api/v1/...",
                                              "error": "Not Found",
                                              "message": "{data} not found"
                                            }
                                            """
                            ))})
    })
    public ErrorResponse handleResourceNotFoundException(ResourceNotFoundException e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(new Date());
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
        errorResponse.setStatus(NOT_FOUND.value());
        errorResponse.setError(NOT_FOUND.getReasonPhrase());
        errorResponse.setMessage(e.getMessage());

        return errorResponse;
    }

    @ExceptionHandler(InvalidDataException.class)
    @ResponseStatus(CONFLICT)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "409", description = "Conflict",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "409 Response",
                                    summary = "Handle exception when input data is conflicted",
                                    value = """
                                            {
                                              "timestamp": "2023-10-19T06:07:35.321+00:00",
                                              "status": 409,
                                              "path": "/api/v1/...",
                                              "error": "Conflict",
                                              "message": "{data} exists, Please try again!"
                                            }
                                            """
                            ))})
    })
    public ErrorResponse handleDuplicateKeyException(InvalidDataException e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(new Date());
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
        errorResponse.setStatus(CONFLICT.value());
        errorResponse.setError(CONFLICT.getReasonPhrase());
        errorResponse.setMessage(e.getMessage());

        return errorResponse;
    }

    @ExceptionHandler(AmazonS3Exception.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request for S3",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "400 S3 Response",
                                    summary = "Handle S3 client errors (e.g., NoSuchKey, AccessDenied)",
                                    value = """
                                            {
                                              "timestamp": "2025-05-20T08:53:00.000+07:00",
                                              "status": 400,
                                              "path": "/api/rooms",
                                              "error": "S3 Client Error",
                                              "message": "File not found on S3"
                                            }
                                            """
                            ))}),
            @ApiResponse(responseCode = "403", description = "Forbidden for S3",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "403 S3 Response",
                                    summary = "Handle S3 access denied errors",
                                    value = """
                                            {
                                              "timestamp": "2025-05-20T08:53:00.000+07:00",
                                              "status": 403,
                                              "path": "/api/rooms",
                                              "error": "S3 Access Denied",
                                              "message": "Access denied to S3 resource"
                                            }
                                            """
                            ))}),
            @ApiResponse(responseCode = "500", description = "S3 Server Error",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "500 S3 Response",
                                    summary = "Handle S3 server errors",
                                    value = """
                                            {
                                              "timestamp": "2025-05-20T08:53:00.000+07:00",
                                              "status": 500,
                                              "path": "/api/rooms",
                                              "error": "S3 Server Error",
                                              "message": "S3 service unavailable"
                                            }
                                            """
                            ))})
    })
    public ErrorResponse handleAmazonS3Exception(AmazonS3Exception e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(new Date());
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));

        // Phân loại lỗi dựa trên mã lỗi S3
        String errorCode = e.getErrorCode();
        switch (errorCode) {
            case "NoSuchKey":
                errorResponse.setStatus(BAD_REQUEST.value());
                errorResponse.setError("S3 Client Error");
                errorResponse.setMessage("File not found on S3");
                break;
            case "AccessDenied":
                errorResponse.setStatus(FORBIDDEN.value());
                errorResponse.setError("S3 Access Denied");
                errorResponse.setMessage("Access denied to S3 resource");
                break;
            case "NoSuchBucket":
                errorResponse.setStatus(BAD_REQUEST.value());
                errorResponse.setError("S3 Client Error");
                errorResponse.setMessage("S3 bucket does not exist");
                break;
            default:
                errorResponse.setStatus(INTERNAL_SERVER_ERROR.value());
                errorResponse.setError("S3 Server Error");
                errorResponse.setMessage("S3 service error: " + e.getMessage());
                break;
        }

        return errorResponse;
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500", description = "File Processing Error",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "500 File Processing Response",
                                    summary = "Handle file processing errors",
                                    value = """
                                            {
                                              "timestamp": "2025-05-20T08:53:00.000+07:00",
                                              "status": 500,
                                              "path": "/api/rooms",
                                              "error": "File Processing Error",
                                              "message": "Error processing file"
                                            }
                                            """
                            ))})
    })
    public ErrorResponse handleIOException(IOException e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(new Date());
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
        errorResponse.setStatus(INTERNAL_SERVER_ERROR.value());
        errorResponse.setError("File Processing Error");
        errorResponse.setMessage("Error processing file: " + e.getMessage());
        return errorResponse;
    }



    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(BAD_REQUEST)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "400 Response",
                                    summary = "Handle bad request exception",
                                    value = """
                        {
                          "timestamp": "2023-10-19T06:35:52.333+00:00",
                          "status": 400,
                          "path": "/api/v1/...",
                          "error": "Bad Request",
                          "message": "Invalid input provided"
                        }
                        """
                            ))})
    })
    public ErrorResponse handleBadRequest(BadRequestException e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(new Date());
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
        errorResponse.setStatus(BAD_REQUEST.value());
        errorResponse.setError(BAD_REQUEST.getReasonPhrase());
        errorResponse.setMessage(e.getMessage());

        return errorResponse;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "500 Response",
                                    summary = "Handle exception when internal server error",
                                    value = """
                                            {
                                              "timestamp": "2023-10-19T06:35:52.333+00:00",
                                              "status": 500,
                                              "path": "/api/v1/...",
                                              "error": "Internal Server Error",
                                              "message": "Connection timeout, please try again"
                                            }
                                            """
                            ))})
    })

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleWebSocketException(Exception exception) {
        log.error("WebSocket error: {}", exception.getMessage(), exception);
        return "Error: " + exception.getMessage();
    }

    public ErrorResponse handleException(Exception e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(new Date());
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
        errorResponse.setStatus(INTERNAL_SERVER_ERROR.value());
        errorResponse.setError(INTERNAL_SERVER_ERROR.getReasonPhrase());
        errorResponse.setMessage(e.getMessage());

        return errorResponse;
    }
}
