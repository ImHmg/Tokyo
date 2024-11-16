package io.github.imhmg.tokyo.util;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProductsMockAPI extends Dispatcher {

    private final Map<String, Product> products = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String validToken;

    @Override
    public MockResponse dispatch(RecordedRequest request) {
        String path = request.getPath();
        String method = request.getMethod();

        try {
            if (path.equals("/login") && "POST".equalsIgnoreCase(method)) {
                return handleLogin();
            } else if (path.equals("/products/add") && "POST".equalsIgnoreCase(method)) {
                return handleAddProduct(request);
            } else if (path.equals("/products/remove") && "POST".equalsIgnoreCase(method)) {
                return handleRemoveProduct(request);
            } else if (path.equals("/products/update") && "POST".equalsIgnoreCase(method)) {
                return handleUpdateProduct(request);
            } else if (path.startsWith("/products/get/") && "GET".equalsIgnoreCase(method)) {
                return handleGetProduct(request);
            } else if (path.equals("/products/getAll") && "GET".equalsIgnoreCase(method)) {
                return handleGetAllProducts(request);
            }
        } catch (Exception e) {
            return new MockResponse()
                    .setResponseCode(500)
                    .setBody(String.format("{\"error\": \"Internal server error: %s\"}", e.getMessage()));
        }

        return new MockResponse().setResponseCode(404).setBody("{\"error\": \"Endpoint not found\"}");
    }

    private MockResponse handleLogin() {
        validToken = UUID.randomUUID().toString();
        return new MockResponse()
                .setResponseCode(200)
                .addHeader("Authorization", validToken)
                .setBody("{\"status\": \"success\"}");
    }

    private boolean isAuthorized(RecordedRequest request) {
        String token = request.getHeader("Authorization");
        return token != null && token.equals(validToken);
    }

    private MockResponse handleAddProduct(RecordedRequest request) {
        if (!isAuthorized(request)) {
            return unauthorizedResponse();
        }

        try {
            ObjectNode requestBody = objectMapper.readValue(request.getBody().readUtf8(), ObjectNode.class);
            String id = requestBody.get("id").asText();
            String name = requestBody.get("name").asText();
            String price = requestBody.get("price").asText();
            String stock = requestBody.get("stock").asText();
            String supplierName = requestBody.get("supplierName").asText();

            if (products.containsKey(id)) {
                return new MockResponse()
                        .setResponseCode(409)
                        .setBody(String.format("{\"error\": \"Product with id '%s' already exists\"}", id));
            }

            products.put(id, new Product(id, name, price, stock, supplierName));
            return new MockResponse()
                    .setResponseCode(200)
                    .setBody(objectMapper.writeValueAsString(products.get(id)));
        } catch (Exception e) {
            return new MockResponse().setResponseCode(400).setBody("{\"error\": \"Invalid JSON body\"}");
        }
    }

    private MockResponse handleRemoveProduct(RecordedRequest request) {
        if (!isAuthorized(request)) {
            return unauthorizedResponse();
        }

        try {
            ObjectNode requestBody = objectMapper.readValue(request.getBody().readUtf8(), ObjectNode.class);
            String id = requestBody.get("id").asText();

            if (products.containsKey(id)) {
                products.remove(id);
                return new MockResponse().setResponseCode(200).setBody("{\"status\": \"success\"}");
            } else {
                return new MockResponse()
                        .setResponseCode(404)
                        .setBody(String.format("{\"error\": \"Product with id '%s' not found\"}", id));
            }
        } catch (Exception e) {
            return new MockResponse().setResponseCode(400).setBody("{\"error\": \"Invalid JSON body\"}");
        }
    }

    private MockResponse handleUpdateProduct(RecordedRequest request) {
        if (!isAuthorized(request)) {
            return unauthorizedResponse();
        }

        try {
            ObjectNode requestBody = objectMapper.readValue(request.getBody().readUtf8(), ObjectNode.class);
            String id = requestBody.get("id").asText();
            String name = requestBody.get("name").asText();
            String price = requestBody.get("price").asText();
            String stock = requestBody.get("stock").asText();
            String supplierName = requestBody.get("supplierName").asText();

            if (products.containsKey(id)) {
                products.put(id, new Product(id, name, price, stock, supplierName));
                return new MockResponse()
                        .setResponseCode(200)
                        .setBody(objectMapper.writeValueAsString(products.get(id)));
            } else {
                return new MockResponse()
                        .setResponseCode(404)
                        .setBody(String.format("{\"error\": \"Product with id '%s' not found\"}", id));
            }
        } catch (Exception e) {
            return new MockResponse().setResponseCode(400).setBody("{\"error\": \"Invalid JSON body\"}");
        }
    }

    private MockResponse handleGetProduct(RecordedRequest request) {
        if (!isAuthorized(request)) {
            return unauthorizedResponse();
        }
        String path = request.getPath();
        String id = path.substring(path.lastIndexOf("/") + 1);
        if (products.containsKey(id)) {
            try {
                return new MockResponse()
                        .setResponseCode(200)
                        .setBody(objectMapper.writeValueAsString(products.get(id)));
            } catch (Exception e) {
                return new MockResponse().setResponseCode(500).setBody("{\"error\": \"Error generating JSON response\"}");
            }
        }
        return new MockResponse().setResponseCode(404).setBody(String.format("{\"error\": \"Product with id '%s' not found\"}", id));
    }

    private MockResponse handleGetAllProducts(RecordedRequest request) {
        if (!isAuthorized(request)) {
            return unauthorizedResponse();
        }

        try {
            return new MockResponse()
                    .setResponseCode(200)
                    .setBody(objectMapper.writeValueAsString(products.values()));
        } catch (Exception e) {
            return new MockResponse().setResponseCode(500).setBody("{\"error\": \"Error generating JSON response\"}");
        }
    }

    private MockResponse unauthorizedResponse() {
        return new MockResponse().setResponseCode(401).setBody("{\"error\": \"Unauthorized\"}");
    }

    private static class Product {
        private final String id;
        private final String name;
        private final String price;
        private final String stock;
        private final String supplierName;

        public Product(String id, String name, String price, String stock, String supplierName) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.stock = stock;
            this.supplierName = supplierName;
        }

        // Getters for JSON serialization
        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getPrice() {
            return price;
        }

        public String getStock() {
            return stock;
        }

        public String getSupplierName() {
            return supplierName;
        }
    }
}
