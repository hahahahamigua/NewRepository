import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class TestSalesforceRestful {
	// Need conduct data persistence for the accessToken and instanceUri
	private static String accessToken = null;
	private static String instanceUri = null;
	// 3.1.1 Input Parameter UserName
	private static final String USERNAME = "shyung@cmrs.com.hk.demohk99";
	// 3.1.1 Input Parameter PassWord
	private static final String PASSWORD = "Welcome123!";
	// 3.1.1 End Point
	private static final String END_POINT_AUTH = "https://test.salesforce.com/services/oauth2/token?";
	private static String END_POINT_MEMBER_VALIDATE = null;
	private static String END_POINT_ONLINE_MODE = null;
	private static String END_POINT_OFFLINE_QUERY = null;
	private static String END_POINT_OFFLINE_UPDATE = null;
	//test01
	//test02
	// 3.1.1 Input Parameter Client_ID
	private static final String CLIENTID = "3MVG910YPh8zrcR1yVy5MQ5lXnXsPH9vhm03rjL_pR9XBrF9Vauaesfy0ROFA4e8yJ2gpLoZcs5EcCj.Z_39M";
	// 3.1.1 Input Parameter Client_Secret
	private static final String CLIENTSECRET = "8073820252173015083";
	// 3.1.1 Input Parameter Grant_Type
	private static final String GRANT_TYPE = "password";

	private static final String REST_ENDPOINT_QUERY = "/services/data/v40.0/query";

	// need to connect SFDC and get Token
	private static Header oauthHeader;
	// query size
	private static Header querySizeHeader;
	// content Type
	private static Header contentTypeHeader;

	private static final String BATCHSIZE_QUERY = "200";
	private static final int BATCHSIZE_UPDATE = 200;
	private static final String STR_CONNTENT_TYPE = "application/json";
	private static final String SOQL_QUERY = "?q=SELECT+" + "ID+,+" + "Mobile_Phone__c+,+" + "Facebook_ID__c+,+"
			+ "Member_ID__c+,+" + "IsEligibility__c+,+" + "IsBinding__c+,+" + "BabyInfo__c+"
			+ "from+" + "Facebook_User_Staging__c+"
			+ "where+" + "I_Status__c+=+'New'";

	public static void main(String[] args) throws JSONException, ClientProtocolException, IOException {
		List<String> IDList = null;
		// Interface Document Authentication
		if (initConnect()) {
			// Interface Document Membership Validation
			//memberShipValidation();
			// Interface Document Free Trial Promotion Eligibility Online Mode
			//onlineMode();
			// Interface Document Free Trial Promotion OffLine Mode (Query)
			IDList = queryData();
			// Interface Document Free Trial Promotion OffLine Mode (Update)
			//updateData(IDList);
		}
	}

	/**
	 * distinguish whether can access SFDC or not Method: POST End Point : loginURL
	 * Interface Document [3.1]
	 * 
	 * @return
	 */
	private static boolean initConnect() {
		System.out.println("****************** initConnect() entry ******************");
		HttpClient httpClient = HttpClientBuilder.create().build();
		// Assemble the login request URL
		// Params [Client_Id][Client_Secret][UserName][PassWord][Grant_Type]
		String loginURL = END_POINT_AUTH + "?" + "&client_id=" + CLIENTID + "&client_secret=" + CLIENTSECRET
				+ "&username=" + USERNAME + "&password=" + PASSWORD + "&grant_type=" + GRANT_TYPE;
		// Login requests must be POSTs
		HttpPost httpPost = new HttpPost(loginURL);
		HttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);
		} catch (ClientProtocolException cpException) {
			cpException.printStackTrace();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
		String getResult = null;
		try {
			getResult = EntityUtils.toString(response.getEntity());
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		// verify response is HTTP OK
		final int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != HttpStatus.SC_OK) {
			System.out.println("Error authenticating to Force.com:" + statusCode);
			System.out.println("Response Result: " + getResult);
			return false;
		}
		JSONObject jsonObject = null;
		try {
			jsonObject = (JSONObject) new JSONTokener(getResult).nextValue();
			accessToken = jsonObject.getString("access_token");
			instanceUri = jsonObject.getString("instance_url");
			END_POINT_MEMBER_VALIDATE = instanceUri + "/services/apexrest/MembershipValidate";
			END_POINT_ONLINE_MODE = instanceUri + "/services/apexrest/FBFreeTrialCheck";
			END_POINT_OFFLINE_QUERY = instanceUri + REST_ENDPOINT_QUERY + SOQL_QUERY;
			END_POINT_OFFLINE_UPDATE = instanceUri + "/services/apexrest/FBStatusUpdateBack";
		} catch (JSONException jsonException) {
			jsonException.printStackTrace();
		}
		// Get Token
		oauthHeader = new BasicHeader("Authorization", "Bearer " + accessToken);
		// Set the query batch size is 200
		querySizeHeader = new BasicHeader("Sforce-Query-Options", "batchSize=" + BATCHSIZE_QUERY);
		// Set contentTpye
		contentTypeHeader = new BasicHeader("Content-Type", STR_CONNTENT_TYPE);

		System.out.println("oauthHeader:" + oauthHeader);
		System.out.println("response:" + response.getStatusLine());
		System.out.println("Successful login");
		System.out.println("instance URL:" + instanceUri);
		System.out.println("access token/sessing ID:" + accessToken);
		return true;
	}

	/**
	 * Interface Document Membership Validation Web Service
	 * 
	 * @return
	 */
	private static String memberShipValidation() throws JSONException, ClientProtocolException, IOException {
		System.out.println("****************** memberShipValidation() entry ******************");
		String result = null;
		try {
			Boolean isVaildAccessToken = true;
			do {
				if (oauthHeader != null) {
					JSONObject jsonObjectID = new JSONObject();
					jsonObjectID.put("phoneNumber", "99119911");
					jsonObjectID.put("dateOfBirth", "2018-01-01");
					System.out.println(jsonObjectID.toString());
					HttpPost httpPost = new HttpPost(END_POINT_MEMBER_VALIDATE);
					httpPost.addHeader(oauthHeader);
					httpPost.addHeader(contentTypeHeader);
					StringEntity body = new StringEntity(jsonObjectID.toString());
					body.setContentType(STR_CONNTENT_TYPE);
					httpPost.setEntity(body);
					HttpClient httpClient = HttpClientBuilder.create().build();
					HttpResponse response = httpClient.execute(httpPost);
					result = EntityUtils.toString(response.getEntity());
					System.out.println("response :" + result);
					isVaildAccessToken = checkAccessToken(response, result);
				}
			} while (!isVaildAccessToken);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	/**
	 * Interface Document Free Trial Promotion Online Mode
	 * 
	 * @return
	 */
	private static String onlineMode() throws JSONException, ClientProtocolException, IOException {
		System.out.println("****************** onlineMode() entry ******************");
		String result = null;
		try {
			Boolean isVaildAccessToken = true;
			do {
				JSONObject jsonObjectID = new JSONObject();
				jsonObjectID.put("contactID", "0030k00000J81VE");
				jsonObjectID.put("facebookID", "179745182062082");
				jsonObjectID.put("firstName", "Tim");
				jsonObjectID.put("lastName", "Green");
				System.out.println(jsonObjectID.toString());
				HttpPost httpPost = new HttpPost(END_POINT_ONLINE_MODE);
				httpPost.addHeader(oauthHeader);
				httpPost.addHeader(contentTypeHeader);
				StringEntity body = new StringEntity(jsonObjectID.toString());
				body.setContentType(STR_CONNTENT_TYPE);
				httpPost.setEntity(body);
				HttpClient httpClient = HttpClientBuilder.create().build();
				HttpResponse response = httpClient.execute(httpPost);
				result = EntityUtils.toString(response.getEntity());
				System.out.println("response :" + result);
				isVaildAccessToken = checkAccessToken(response, result);
			} while (!isVaildAccessToken);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	/**
	 * Interface Document Free Trial Promotion OffLine Mode (Query)
	 * 
	 * @return
	 */

	private static List<String> queryData() {
		System.out.println("****************** OffLine Mode() queryData ******************");
		List<String> IDList = new ArrayList<String>();
		if (initConnect()) {
			// End Point
			String uri = null;
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet get = new HttpGet(END_POINT_OFFLINE_QUERY);
			// Set the query size Header
			get.setHeader(querySizeHeader);
			// Set the token Authorization
			get.setHeader(oauthHeader);
			// Set contentTpye
			get.setHeader(contentTypeHeader);
			try {
				// Check the query size is >200 or <200
				boolean done = false;
				while (!done) {
					HttpResponse response = client.execute(get);
					int statusCode = response.getStatusLine().getStatusCode();
					if (statusCode == HttpStatus.SC_OK) {
						String response_string = EntityUtils.toString(response.getEntity());
						JSONObject json = new JSONObject(response_string);
						JSONArray results = json.getJSONArray("records");
						String jsonMore = null;
						// If nextRecordsUrl is true, need to request to query next batch
						System.out.println("*************** " + json.getString("done"));
						if (json.getString("done").equals("false")) {
							jsonMore = json.get("nextRecordsUrl").toString();
							// System.out.println("nextRecordsUrl: "+ jsonMore);
							uri = instanceUri + jsonMore;
							// System.out.println("--------------------------queryMore :"+ uri);
							get = new HttpGet(uri);
							get.setHeader(oauthHeader);
							get.setHeader(querySizeHeader);
							get.setHeader(contentTypeHeader);
						} else {
							done = true;
						}
						JSONObject jsonObject = null;
						for (int i = 0; i < results.length(); i++) {
							jsonObject = results.getJSONObject(i);
							if (jsonObject != null) {
								System.out.println(" -------------------Facebook User Staging -----------");
								System.out.println(" ID: 		        	" + jsonObject.getString("Id"));
								System.out.println(" Mobile_Phone__c: 		" + jsonObject.getString("Mobile_Phone__c"));
								System.out.println(" Facebook_ID__c: 		" + jsonObject.getString("Facebook_ID__c"));
								System.out.println(" Member_ID__c: 			" + jsonObject.getString("Member_ID__c"));
								System.out.println(" IsEligibility__c: 		" + jsonObject.getString("IsEligibility__c"));
								System.out.println(" IsBinding__c: 			" + jsonObject.getString("IsBinding__c"));
								System.out.println(" BabyInfo__c: 			" + jsonObject.getString("BabyInfo__c"));
								/*-----------------------  BabyInfo__c Parse Start ------------------------------*/ 
								String babyInfoJSON = jsonObject.getString("BabyInfo__c").replace("'", "\"");
								System.out.println(" babyInfoJSON: 			" + babyInfoJSON);
								JSONObject jsonBabyInfo = new JSONObject(babyInfoJSON);
								JSONArray resultsBabyInfo = jsonBabyInfo.getJSONArray("babies");
								JSONObject jsonOBI = null;
								for (int j = 0; j < resultsBabyInfo.length(); j++) {
									jsonOBI = resultsBabyInfo.getJSONObject(j);
									System.out.println(" -------------------BabyInfo__c babies Parse-----------");
									System.out.println(" ID: 		        " + jsonOBI.getString("Id"));
									System.out.println(" BabyBirthday: 		" + jsonOBI.getString("BabyBirthday"));
									System.out.println(" offerStatus: 		" + jsonOBI.getString("offerStatus"));
									JSONArray resultsOfferStatus = (JSONArray) new JSONArray(jsonOBI.getString("offerStatus"));
									/*---------------------- OfferStatus Parse Start --------------------------*/
									JSONObject jsonOOS = null;
									for (int k = 0; k < resultsOfferStatus.length(); k++) {
										jsonOOS = resultsOfferStatus.getJSONObject(k);
										System.out.println(" stage:         " + jsonOOS.getString("stage"));
										System.out.println(" isEnjoyed: 	" + jsonOOS.getString("isEnjoyed"));
									}
									/*---------------------- OfferStatus Parse End   --------------------------*/
								}
								/*-----------------------  BabyInfo__c Parse End ------------------------------*/ 
								// Put the IDS into the IDList ,then prepare to update
								String fbID = jsonObject.getString("Id");
								IDList.add(fbID);
							}
						}
					}
				}
				System.out.println("IDList.size():" + IDList.size());
			} catch (JSONException e) {
				System.out.println("Issue creating JSON or processing results");
				e.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} catch (NullPointerException npe) {
				npe.printStackTrace();
			}
		}
		return IDList;
	}

	/**
	 * Interface Document Free Trial Promotion OffLine Mode (Update)
	 * 
	 * @return
	 */
	private static List<String> updateData(List<String> IDList)
			throws JSONException, ClientProtocolException, IOException {
		List<String> result = new ArrayList<String>();
		try {
			if (initConnect()) {
				JSONObject jsonObjectID = new JSONObject();
				JSONArray jsonArrayID = new JSONArray();
				Map<String, String> mapID = new HashMap<String, String>();
				int flag = 0;
				for (int i = 0; i < IDList.size(); i++) {
					mapID.put("Id", IDList.get(i));
					jsonArrayID.put(mapID);
					flag = i + 1;
					// batch size is 200 200/per to request to call WebService
					if ((flag != 1 && flag % BATCHSIZE_UPDATE == 0) || (flag == IDList.size())) {
						jsonObjectID.put("records", jsonArrayID);
						System.out.println(jsonObjectID.toString());
						// String uri = instanceUri + "/services/apexrest/FBStatusUpdateBack";
						HttpPost httpPost = new HttpPost(END_POINT_OFFLINE_UPDATE);
						httpPost.addHeader(oauthHeader);
						httpPost.addHeader(contentTypeHeader);
						StringEntity body = new StringEntity(jsonObjectID.toString());
						body.setContentType(STR_CONNTENT_TYPE);
						httpPost.setEntity(body);
						HttpClient httpClient = HttpClientBuilder.create().build();
						HttpResponse response = httpClient.execute(httpPost);
						String getResult = EntityUtils.toString(response.getEntity());
						System.out.println("response :" + getResult);
						jsonArrayID = new JSONArray();
						result.add(getResult);
					}
				}
			}
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}
		return result;
	}

	private static boolean checkAccessToken(HttpResponse response, String result) {
		boolean isVaild = false;
		// verify response is HTTP OK
		final int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != HttpStatus.SC_OK) {
			System.out.println("Error authenticating to Force.com:" + statusCode);
			try {
				JSONObject jsonObject = null;
				if (result.startsWith("[") && result.endsWith("]")) {
					result = result.substring(1, result.length() - 1);
				}
				jsonObject = (JSONObject) new JSONTokener(result).nextValue();
				String message = jsonObject.getString("message");
				String errorCode = jsonObject.getString("errorCode");
				if ("INVALID_SESSION_ID".equalsIgnoreCase(errorCode)) {
					isVaild = false;
					initConnect();
				}
			} catch (JSONException jsonException) {
				jsonException.printStackTrace();
			}
		} else {
			isVaild = true;
		}
		return isVaild;
	}
}