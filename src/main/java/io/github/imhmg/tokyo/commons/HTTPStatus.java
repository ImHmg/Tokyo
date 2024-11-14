package io.github.imhmg.tokyo.commons;

import java.util.HashMap;
import java.util.Map;

public class HTTPStatus {
    public static Map<Integer, String> httpStatusMap = new HashMap<>();

    static {
        // 1xx Informational
        httpStatusMap.put(100, "Continue");
        httpStatusMap.put(101, "Switching Protocols");
        httpStatusMap.put(102, "Processing (WebDAV)");
        httpStatusMap.put(103, "Early Hints");

        // 2xx Success
        httpStatusMap.put(200, "OK");
        httpStatusMap.put(201, "Created");
        httpStatusMap.put(202, "Accepted");
        httpStatusMap.put(203, "Non-Authoritative Information");
        httpStatusMap.put(204, "No Content");
        httpStatusMap.put(205, "Reset Content");
        httpStatusMap.put(206, "Partial Content");
        httpStatusMap.put(207, "Multi-Status (WebDAV)");
        httpStatusMap.put(208, "Already Reported (WebDAV)");
        httpStatusMap.put(226, "IM Used");

        // 3xx Redirection
        httpStatusMap.put(300, "Multiple Choices");
        httpStatusMap.put(301, "Moved Permanently");
        httpStatusMap.put(302, "Found");
        httpStatusMap.put(303, "See Other");
        httpStatusMap.put(304, "Not Modified");
        httpStatusMap.put(305, "Use Proxy");
        httpStatusMap.put(307, "Temporary Redirect");
        httpStatusMap.put(308, "Permanent Redirect");

        // 4xx Client Error
        httpStatusMap.put(400, "Bad Request");
        httpStatusMap.put(401, "Unauthorized");
        httpStatusMap.put(402, "Payment Required");
        httpStatusMap.put(403, "Forbidden");
        httpStatusMap.put(404, "Not Found");
        httpStatusMap.put(405, "Method Not Allowed");
        httpStatusMap.put(406, "Not Acceptable");
        httpStatusMap.put(407, "Proxy Authentication Required");
        httpStatusMap.put(408, "Request Timeout");
        httpStatusMap.put(409, "Conflict");
        httpStatusMap.put(410, "Gone");
        httpStatusMap.put(411, "Length Required");
        httpStatusMap.put(412, "Precondition Failed");
        httpStatusMap.put(413, "Payload Too Large");
        httpStatusMap.put(414, "URI Too Long");
        httpStatusMap.put(415, "Unsupported Media Type");
        httpStatusMap.put(416, "Range Not Satisfiable");
        httpStatusMap.put(417, "Expectation Failed");
        httpStatusMap.put(418, "I'm a teapot");
        httpStatusMap.put(421, "Misdirected Request");
        httpStatusMap.put(422, "Unprocessable Entity (WebDAV)");
        httpStatusMap.put(423, "Locked (WebDAV)");
        httpStatusMap.put(424, "Failed Dependency (WebDAV)");
        httpStatusMap.put(425, "Too Early");
        httpStatusMap.put(426, "Upgrade Required");
        httpStatusMap.put(428, "Precondition Required");
        httpStatusMap.put(429, "Too Many Requests");
        httpStatusMap.put(431, "Request Header Fields Too Large");
        httpStatusMap.put(451, "Unavailable For Legal Reasons");

        // 5xx Server Error
        httpStatusMap.put(500, "Internal Server Error");
        httpStatusMap.put(501, "Not Implemented");
        httpStatusMap.put(502, "Bad Gateway");
        httpStatusMap.put(503, "Service Unavailable");
        httpStatusMap.put(504, "Gateway Timeout");
        httpStatusMap.put(505, "HTTP Version Not Supported");
        httpStatusMap.put(506, "Variant Also Negotiates");
        httpStatusMap.put(507, "Insufficient Storage (WebDAV)");
        httpStatusMap.put(508, "Loop Detected (WebDAV)");
        httpStatusMap.put(510, "Not Extended");
        httpStatusMap.put(511, "Network Authentication Required");
    }
}
