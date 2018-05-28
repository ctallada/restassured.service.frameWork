package com.osi.platform.services;


import static com.jayway.restassured.RestAssured.expect;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

import cucumber.api.DataTable;


public class RestAssuredService {

    private static Logger logger = LoggerFactory.getLogger(RestAssuredService.class);
    public static ApplicationContext applicationContext;
    @Autowired
    private static Environment env;
    static AnnotationConfigApplicationContext annotationConfigApplicationContext;
    static DataSource dataSource;
    static Connection dataBaseConnection;
    public static JdbcTemplate jdbcTemplate;
    
    public static String CUSTOMER_HOST;
    public static String ORDER_HOST;
    public static String FCC_HOST;
    public static String PREFERENCES_HUB_HOST;
    public static String PREFERENCES_XAPI_HOST;
    public static String LOYALTY_XAPI_HOST;
	public static String OTP_XAPI_HOST;
    public static String END_POINT;
    public static String RESOURCES_PATH_URL = "";
    public String expectedResponse = "";
    public String responsType = "";
    public String requestMethod = "";
    static RestAssuredService restAssuredService = null;

    Properties properties = new Properties();
    static ObjectMapper mapper = new ObjectMapper();

    public RestAssuredService() {

    }
    
    public static RestAssuredService getInstance(Properties properties) {
		if (restAssuredService == null) {
			restAssuredService = new RestAssuredService(properties);
		}
		return restAssuredService;
	}

	public RestAssuredService(Properties properties) {
		this.properties = properties;
		setHostDetails();
		// jdbcTemplate = getJdbcTemplate(properties);
		if (!Boolean.parseBoolean(properties.getProperty(RestAssured.USE_WIRE_MOCK_SERVER))) {
			//jdbcTemplate = getJdbcTemplate(properties);
		}
	}

    private JdbcTemplate getJdbcTemplate(Properties properties) {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(properties.getProperty(RestAssured.MACYS_DATABASE_DRIVER));
        basicDataSource.setUrl(properties.getProperty(RestAssured.MACYS_DATABASE_URL));
        basicDataSource.setUsername(properties.getProperty(RestAssured.MACYS_DATABASE_USER_NAME));
        basicDataSource.setPassword(properties.getProperty(RestAssured.MACYS_DATABASE_PASSWORD));
        System.out.println("macys.database.driver::: "+properties.getProperty(RestAssured.MACYS_DATABASE_DRIVER));
        System.out.println("macys.database.url::: "+properties.getProperty(RestAssured.MACYS_DATABASE_URL));
        System.out.println("macys.database.username::: "+properties.getProperty(RestAssured.MACYS_DATABASE_USER_NAME));
        System.out.println("macys.database.password::: "+properties.getProperty(RestAssured.MACYS_DATABASE_PASSWORD));
        System.out.println("resources path::: "+properties.getProperty(RestAssured.RESOURCES_PATH));
        try {
            dataBaseConnection = basicDataSource.getConnection();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        jdbcTemplate = new JdbcTemplate(basicDataSource);
        return jdbcTemplate;
    }

    private void setHostDetails() {
        boolean isCustomerDomain = Boolean.parseBoolean(properties.getProperty(RestAssured.IS_CUSOTMER_DOMAIN));
        boolean isOrderDomain = Boolean.parseBoolean(properties.getProperty(RestAssured.IS_ORDER_DOMAIN));
        boolean isPreferencesHubDomain = Boolean.parseBoolean(properties.getProperty(RestAssured.IS_PREFERENCES_HUB_DOMAIN));
        boolean isPreferencesXapiDomain = Boolean.parseBoolean(properties.getProperty(RestAssured.IS_PREFERENCES_XAPI_DOMAIN));
        boolean isLoyaltyXapiDomain = Boolean.parseBoolean(properties.getProperty(RestAssured.IS_LOYALTY_XAPI_DOMAIN));
		boolean isOtpXapiDomain = Boolean.parseBoolean(properties.getProperty(RestAssured.IS_OTP_XAPI_DOMAIN));

        CUSTOMER_HOST = properties.getProperty(RestAssured.CUSTOMER_END_POINT);
        ORDER_HOST = properties.getProperty(RestAssured.ORDER_END_POINT);
        FCC_HOST = properties.getProperty(RestAssured.FCC_END_POINT);
        PREFERENCES_HUB_HOST = properties.getProperty(RestAssured.PREFERENCES_HUB_END_POINT);
        PREFERENCES_XAPI_HOST = properties.getProperty(RestAssured.PREFERENCES_XAPI_END_POINT);
        LOYALTY_XAPI_HOST = properties.getProperty(RestAssured.LOYALTY_XAPI_END_POINT);
		OTP_XAPI_HOST = properties.getProperty(RestAssured.OTP_XAPI_END_POINT);
        RESOURCES_PATH_URL = properties.getProperty(RestAssured.RESOURCES_PATH);

        if (isCustomerDomain) {
            END_POINT = CUSTOMER_HOST;
        } else if (isOrderDomain) {
            END_POINT = ORDER_HOST;
        } else if (isPreferencesHubDomain) {
            END_POINT = PREFERENCES_HUB_HOST;
        } else if (isPreferencesXapiDomain) {
            END_POINT = PREFERENCES_XAPI_HOST;
        } else if (isLoyaltyXapiDomain) {
            END_POINT = LOYALTY_XAPI_HOST;
        } else if (isOtpXapiDomain) {
            END_POINT = OTP_XAPI_HOST;
        }
        logger.info("*******************END POINT************" + END_POINT);
        System.out.println("*******************END POINT************" + END_POINT);
        System.out.println("*******************CUSTOMER_HOST************" + CUSTOMER_HOST);
    }


    public JdbcTemplate getJdbcTemplate() {

        try {

            jdbcTemplate = new JdbcTemplate(dataSource);
        } catch (Exception e) {
            // logger.error("Exception :: getJdbcTemplate : ", e);
        }

        return jdbcTemplate;
    }

    //@Test
    public String testSelectData(DataSource dataSource) throws SQLException {

        String selectData = new JdbcTemplate(dataSource).queryForObject(
                "SELECT ADD_BY_APP FROM CART_ITEM WHERE USER_ID='6501'", String.class);

        logger.info(selectData + " data has been inserted.");
        return selectData;

    }

    public Map<String, Object> invokeService(DataTable table, Map<String, Map<String, String>> dynamicMap)
            throws IOException {
        Map<String, Object> map = new HashMap<String, Object>();
        //String actualResponse = "";
        requestMethod = "";
        logger.info("*********invokeService is starting*********");
        RequestSpecification requestSpec = getRequestSpecificationBuildData(table, dynamicMap);

        Response response = invokeActualRestServiceAPI(requestSpec);
        map.put("ACTUAL_RESPONSE", response);
        map.put("EXPECTED_RESPONSE", expectedResponse);
        map.put(RestAssured.RESPONSE_TYPE, responsType);
        logger.info("*********invokeService is ending*********");
        return map;
    }

    private Response invokeActualRestServiceAPI(RequestSpecification requestSpec) {
        Response response = null;

        logger.info("*********invokeActualRestMethod is starting: requestMethod::*********" + requestMethod);
        if (StringUtils.isNotBlank(requestMethod)) {
            if (requestMethod.equals(RequestMethod.GET.toString())) {
                response = expect().given().spec(requestSpec).when().get();
            } else if (requestMethod.equals(RequestMethod.POST.toString())) {
                response = expect().given().spec(requestSpec).when().post();
            } else if (requestMethod.equals(RequestMethod.PUT.toString())) {
                response = expect().given().spec(requestSpec).when().put();
            } else if (requestMethod.equals(RequestMethod.DELETE.toString())) {
                response = expect().given().spec(requestSpec).when().delete();
            } else if (requestMethod.equals(RequestMethod.PATCH.toString())) {
                response = expect().given().spec(requestSpec).when().patch();
            }

        }
        logger.info("*********invokeActualRestMethod is ending*********");
        return response;
    }

    public RequestSpecification getRequestSpecificationBuildData(DataTable table, Map<String, Map<String, String>> dynamicMap) throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        String headerParametersWithSemiColon="";
        String[] queryParameters = null;
        String[] headerParameters = null;
        String[] pathParameters1 = null;
        Map<String, String> headerParametersMap = new HashMap<String, String>();
        Map<String, String> queryParametersMap = new HashMap<String, String>();
        //Map<String, String> pathParametersMap = new HashMap<String, String>();
        Map<String, String> dynamicQueryParameterValuesMap = null;
        Map<String, String> dynamicPathParameterValuesMap = null;
        Map<String, String> dynamicRequestBodyValuesMap = null;
        String endPoint = "";
        String requestBody = "";
        expectedResponse = "";
        responsType = RestAssured.APPLICATION_JSON;

        int tableSize = table.topCells().size();
        for (int i = 0; i < tableSize; i++) {
            map.put(table.getGherkinRows().get(0).getCells().get(i), table.getGherkinRows().get(1).getCells().get(i));
        }

        String uri = map.get(RestAssured.REST_URI);
        requestMethod = map.get(RestAssured.REQUEST_METHOD);
        String userName = map.get(RestAssured.USER_NAME);
        String password = map.get(RestAssured.USER_PASSWORD);
        String requestFile = map.get(RestAssured.REQUEST_FILE);
        String responseFile = map.get(RestAssured.RESPONSE_FILE);
        String serviceDomain = map.get(RestAssured.SERVICE_DOMAIN);
        headerParametersWithSemiColon = map.get(RestAssured.HEADER_PARAMETERS);
        // String pathParameters = map.get(PATH_PARAMETERS);

        if (StringUtils.isNotBlank(map.get(RestAssured.PATH_PARAMETERS))) {
            pathParameters1 = map.get(RestAssured.PATH_PARAMETERS).split(RestAssured.SEMI_COLON);
        }

        if (StringUtils.isNotBlank(map.get(RestAssured.QUERY_PARAMETERS))) {
            queryParameters = map.get(RestAssured.QUERY_PARAMETERS).split(RestAssured.SEMI_COLON);
        }
        
        if (pathParameters1 != null && pathParameters1.length > 0) {
        	uri = getUriWithPathParameters(uri, pathParameters1, userName, password);
        }

        logger.info("Final uri:::::: " + uri);
        
        if (StringUtils.isNotBlank(headerParametersWithSemiColon)) {

        	headerParameters = headerParametersWithSemiColon.split(RestAssured.SEMI_COLON);
    		if (headerParameters != null && headerParameters.length > 0) {
            	headerParametersMap = getHeaderParametersMap(headerParameters, userName, password);
            }
    	
        }
        
        logger.info("HeaderParametersMap :::::: " + headerParametersMap);
        
        logger.info("**********Request file name********** " + requestFile);
        if (StringUtils.isNotBlank(requestFile)) {
            requestBody = getRequestBody(requestFile);
        }
        
        if (dynamicMap != null && dynamicMap.size() > 0) {
        	dynamicQueryParameterValuesMap = dynamicMap.get(RestAssured.DYNAMIC_QUERY_PARAMETERS);
            dynamicPathParameterValuesMap = dynamicMap.get(RestAssured.DYNAMIC_PATH_PARAMETERS);
            dynamicRequestBodyValuesMap = dynamicMap.get(RestAssured.DYNAMIC_REQUEST_BODY);
        }
        
        if(queryParameters != null && queryParameters.length>0){
        	queryParametersMap = getQueryParametersMap(queryParameters,dynamicQueryParameterValuesMap);
        }
        System.out.println("Final QueryParameters:::::   "+queryParametersMap);

        if (StringUtils.isNotBlank(uri)) {
            if (dynamicPathParameterValuesMap != null && dynamicPathParameterValuesMap.size() > 0) {
                uri = updatePathParametersInURI(uri, dynamicPathParameterValuesMap);
            }
        }
        if (StringUtils.isNotBlank(requestBody)) {
            if (dynamicRequestBodyValuesMap != null && dynamicRequestBodyValuesMap.size() > 0) {
                requestBody = updateDynamicRequestBody(requestBody, dynamicRequestBodyValuesMap);
            }
        }
    
        
        if (StringUtils.isNotBlank(responseFile)) {
            expectedResponse = getRequestBody(responseFile);
        }
        
		
		if (StringUtils.isNotBlank(map.get(RestAssured.DYNAMIC_HOST))) {
			System.out.println("DYNAMIC_HOST Part..................");
			endPoint = properties.getProperty(map.get(RestAssured.DYNAMIC_HOST));
		} else {
			endPoint = getEndPoint(serviceDomain);
		}
		
		System.out.println("End Point::::: "+endPoint);
        logger.info("**********Final Final API:::********** " + endPoint + uri);
        RequestSpecBuilder builder = new RequestSpecBuilder();
        builder.setBaseUri(endPoint + uri);
        builder.addHeaders(headerParametersMap);
        builder.addQueryParameters(queryParametersMap);
        if (StringUtils.isNotBlank(requestBody)) {
            builder.setBody(requestBody);
        }
        RequestSpecification requestSpec = builder.build();
        return requestSpec;
    }
    
    
    private String getUriWithPathParameters(String uri, String[] pathParameters1, String userName, String password) {

		for (String pathParameter : pathParameters1) {
            if (StringUtils.isNotBlank(pathParameter)) {
                String[] subString = pathParameter.split(RestAssured.EQUALS_TO);
                uri = uri.replaceFirst(subString[0], subString[1]);
            }
        }
        uri = uri.replace(RestAssured.LEFT_BRACE, RestAssured.EMPTY_STRING);
        uri = uri.replace(RestAssured.RIGHT_BRACE, RestAssured.EMPTY_STRING);
		return uri;
	}

	private Map<String, String> getHeaderParametersMap(String[] headerParameters, String userName, String password) {

		Map<String, String> headerParametersMap = new HashMap<String, String>();
		
        for (String header : headerParameters) {
            if (StringUtils.isNotBlank(header)) {
                    String[] subString = header.split(RestAssured.EQUALS_TO);
                    headerParametersMap.put(subString[0], subString[1]);
                    if (RestAssured.ACCEPT.equalsIgnoreCase(subString[0])) {
                        if (RestAssured.APPLICATION_XML.equalsIgnoreCase(subString[1])) {
                            responsType = RestAssured.APPLICATION_XML;
                        }
                    }
                }
            }
        return headerParametersMap;
      }
		
	
	/*private Map<String, String> getCookieHeaderParametersMap(String[] headerParameters, String userName, String password) {

		Map<String, String> headerParametersMap = new HashMap<String, String>();
		
        for (String header : headerParameters) {
            if (StringUtils.isNotBlank(header)) {
                if (header.contains(RestAssured.X_MACYS_SECURITY_TOKEN)) {
                    headerParametersMap.put(RestAssured.X_MACYS_SECURITY_TOKEN, generateSecureToken(userName, password));
                } else if (header.contains(RestAssured.X_MACYS_USER_GUID)) {
                    headerParametersMap.put(RestAssured.X_MACYS_USER_GUID, generateUserGuid(userName, password));
                } else if (header.contains(RestAssured.X_MACYS_UID)) {
                    headerParametersMap.put(RestAssured.X_MACYS_UID, generateUserId(userName, password));
				} else if (header.toLowerCase().contains(RestAssured.COOKIE.toLowerCase())) {
					String cookieValues = "";
					logger.info("Header Cookie::: " + header);
					String[] subString = header.split(RestAssured.EQUALS_TO, 2);
					// logger.info("Cookie value"+subString[1]);
					if (subString[1] != null) {
						String key = "";
						String value = "";
						String[] cookieNameAndValue = subString[1].split(RestAssured.SEMI_COLON);
						for (String string : cookieNameAndValue) {
							if (string.contains(RestAssured.SECURE_USER_TOKEN)) {
								value = generateSecureToken(userName, password);
								String[] secureTokenKey = string.split(RestAssured.EQUALS_TO);
								key = secureTokenKey[0];
								cookieValues = cookieValues + RestAssured.SEMI_COLON + key + RestAssured.EQUALS_TO + value;
							} else if (string.contains(RestAssured.SNSGCS)) {
								String[] snsgcsKey = string.split(RestAssured.EQUALS_TO);
								key = snsgcsKey[0];
								value = "bypass_session_filter1_92_false3_87_last_access_token1_92_"+ new Date().getTime() + "3_87_loginAttempt1_92_1";
								cookieValues = cookieValues + RestAssured.SEMI_COLON + key + RestAssured.EQUALS_TO + value;
							} else if (string.contains(RestAssured.MACYS_ONLINE_UID)) {
								String[] uidKey = string.split(RestAssured.EQUALS_TO);
								key = uidKey[0];
								value = generateUserId(userName, password);
								cookieValues = cookieValues + RestAssured.SEMI_COLON + key + RestAssured.EQUALS_TO + value;
							} else if (string.contains(RestAssured.FORWARDPAGE_KEY)) {
								String[] forwardPageKey = string.split(RestAssured.EQUALS_TO);
								key = forwardPageKey[0];
								value = "https://192.168.60.52:8081/service/order-status?cm_sp=navigation-_-top_nav-_-my_order_history";
								cookieValues = cookieValues + RestAssured.SEMI_COLON + key + RestAssured.EQUALS_TO + value;
							} else {
								cookieValues = cookieValues + RestAssured.SEMI_COLON + string;
							}
						}

					}
					cookieValues = (cookieValues + RestAssured.SEMI_COLON).substring(1);
					// headerParametersMap.put(COOKIE, subString[1]);
					logger.info("Final Cookie values:::: " + cookieValues);
					headerParametersMap.put(RestAssured.COOKIE, cookieValues);
				} else {
                    String[] subString = header.split(RestAssured.EQUALS_TO);
                    headerParametersMap.put(subString[0], subString[1]);
                    if (RestAssured.ACCEPT.equalsIgnoreCase(subString[0])) {
                        if (RestAssured.APPLICATION_XML.equalsIgnoreCase(subString[1])) {
                            responsType = RestAssured.APPLICATION_XML;
                        }
                    }
                }
            }
        }
		
		return headerParametersMap;
	}*/

    
    
    
	private Map<String, String> getQueryParametersMap(String[] queryParameters, Map<String, String> dynamicQueryParameterValuesMap) {
		 
		logger.info("*******************getQueryParametersMap:::: Starting******************* ");
		Map<String, String> queryParametersMap = new HashMap<String, String>();
		if (dynamicQueryParameterValuesMap == null || dynamicQueryParameterValuesMap.size() == 0) {
				for (String queryParameter : queryParameters) {
					if (StringUtils.isNotBlank(queryParameter)) {
						String[] subString = queryParameter.split(RestAssured.EQUALS_TO);
						queryParametersMap.put(subString[0], subString[1]);
					}
				}
			
		} else {
			List<String> dynamicQueryParametersList = new ArrayList<String>(dynamicQueryParameterValuesMap.keySet());
				for (String queryParameter : queryParameters) {
					if (StringUtils.isNotBlank(queryParameter)) {
						String[] subString = queryParameter.split(RestAssured.EQUALS_TO);
						if (dynamicQueryParametersList.contains(subString[1])) {
							queryParametersMap.put(subString[0],dynamicQueryParameterValuesMap.get(subString[1]));
						} else {
							queryParametersMap.put(subString[0],subString[1]);
						}
					}
				}				
			}

		logger.info("*******************getQueryParametersMap:::: ending*******************queryParametersMap::: "+queryParametersMap);
		return queryParametersMap;
	}

    private String getEndPoint(String serviceDomain) {
        String endPoint = null;
        
        if (StringUtils.isNotBlank(serviceDomain) && serviceDomain.equals(RestAssured.PREFERENCES_HUB_HOST)) {
            endPoint = PREFERENCES_HUB_HOST;
        } else if (StringUtils.isNotBlank(serviceDomain) && serviceDomain.equals(RestAssured.CUSTOMER_HOST)) {
            endPoint = CUSTOMER_HOST;
        } else {
            endPoint = END_POINT;
        }

        return endPoint;
    }

    private String getRequestBody(String requestFile) throws IOException {

        File file = null;
        String body = "";

        logger.info("*********getRequestBody starting**************");
        if (StringUtils.isNotBlank(requestFile)) {
            requestFile = RESOURCES_PATH_URL + requestFile;
            System.out.println("Request File Path URL:::: "+requestFile);
            //URL url = new URL(requestFile);
            try {
                //file = new File(url.toURI());
            	
            	file = new File(requestFile);
            	System.out.println("File Path::::"+file.toPath().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            body = FileUtils.readFileToString(file);
        }
        logger.info("*********getRequestBody ending**************");
        return body;
    }

    private String updateDynamicRequestBody(String requestBody,
                                            Map<String, String> dynamicRequestBodyValuesMap) {
        logger.info("**********updateDynamicRequestBody starting**********");
        for (Map.Entry<String, String> entry : dynamicRequestBodyValuesMap.entrySet()) {
            if (requestBody.contains(entry.getKey())) {
                requestBody = requestBody.replace(entry.getKey(), entry.getValue());
            }
        }
        logger.info("**********updateDynamicRequestBody ending**********" + requestBody);
        return requestBody;
    }

    private String updatePathParametersInURI(String uri, Map<String, String> pathParametersMap) {
        logger.info("**********updatePathParametersInURI**********starting");
        for (Map.Entry<String, String> entry : pathParametersMap.entrySet()) {
            if (uri.contains(entry.getKey())) {
                uri = uri.replaceAll(entry.getKey(), entry.getValue());
            }
        }
        logger.info("Final URI:::::::::::" + uri);
        logger.info("**********updatePathParametersInURI**********ending");
        return uri;
    }

    public long execute(String sql) {
        long id = jdbcTemplate.queryForLong(sql);
        return id;
    }
    
	public String execute(String sql, String type) {
		String value = "";
		if (type.equals("INTEGER") || type.equals("SHORT")) {
			value = String.valueOf(jdbcTemplate.queryForInt(sql));
		} else if (type.equals("LONG")) {
			value = String.valueOf(jdbcTemplate.queryForLong(sql));
		} else if (type.equals("BIG DECIMAL") || type.equals("DOUBLE")) {
			value = String.valueOf(jdbcTemplate.queryForObject(sql, Double.class));
		} else {
			value = jdbcTemplate.queryForObject(sql, String.class);
		}
		return value;
	}

    public String responseContentType() {

        return RestAssured.RESPONSE_TYPE;
    }

    /*public boolean compareErrorCodeResults(Map<String, Object> responseMap) {
        logger.info(" compareErrorCodeResults starting ");
        boolean compare = false;
        Response response = (Response) responseMap.get(RestAssured.ACTUAL_RESPONSE);
        logger.info("Expected Error Response body::\n" + responseMap.get(RestAssured.EXPECTED_RESPONSE).toString());
        logger.info("Actual Error Response body::\n" + response.asString());
        if (responseMap.get(RestAssured.RESPONSE_TYPE).toString().equalsIgnoreCase(RestAssured.APPLICATION_XML)) {
            expectedErrorDO = JAXB.unmarshal(new StringReader(responseMap.get(RestAssured.EXPECTED_RESPONSE).toString()), PlatformErrorsBinding.class);
            actualErrorDO = response.as(PlatformErrorsBinding.class);
            compare = compateExpecteandActualErrorMessage(expectedErrorDO, actualErrorDO);
        } else {
            Object expectedObject = HelperUtility.convertJSONStringToObject(responseMap.get(RestAssured.EXPECTED_RESPONSE).toString(), expectedErrorDO);
            expectedErrorDO = (PlatformErrorsBinding) expectedObject;
            Object object = HelperUtility.convertJSONStringToObject(response.asString(), actualErrorDO);
            actualErrorDO = (PlatformErrorsBinding) object;
        }
        compare = compateExpecteandActualErrorMessage(expectedErrorDO, actualErrorDO);
        logger.info("Assertions status compare Results :::: " + compare);
        logger.info(" compareErrorCodeResults ending ");
        return compare;
    }*/

   /* private boolean compateExpecteandActualErrorMessage(
            PlatformErrorsBinding expectedErrorDO,
            PlatformErrorsBinding actualErrorDO) {
        logger.info("CompateExpecteandActualErrorMessage starting::::::::::::::");
        if (expectedErrorDO != null && actualErrorDO != null) {
            List<PlatformErrorBinding> expectedErrorsList = expectedErrorDO
                    .getError();
            List<PlatformErrorBinding> actualErrorsList = actualErrorDO
                    .getError();
            PlatformErrorBinding expectedErrorBinding = new PlatformErrorBinding();
            PlatformErrorBinding actualErrorBinding = new PlatformErrorBinding();
            if ((expectedErrorsList != null && actualErrorsList != null)
                    && (expectedErrorsList.size() == actualErrorsList.size())) {

                for (int i = 0; i < expectedErrorsList.size(); i++) {
                    expectedErrorBinding = expectedErrorsList.get(i);
                    actualErrorBinding = actualErrorsList.get(i);

                    Assert.assertEquals(expectedErrorBinding.getErrorCode(), actualErrorBinding.getErrorCode());
                    Assert.assertEquals(expectedErrorBinding.getMessage(), actualErrorBinding.getMessage());

                }

            }

        }
        logger.info("CompateExpecteandActualErrorMessage ending::::::::::::::");
        return true;
    }*/

  

    

   

   
    
    

}
