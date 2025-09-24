package org.project.reggie.utils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import lombok.extern.slf4j.Slf4j;

/**
 * 短信发送工具类
 */
@Slf4j
public class SMSUtils {
    // 阿里云访问密钥ID（需要替换为自己的）
    private static final String ACCESS_KEY_ID = "your_access_key_id";
    // 阿里云访问密钥Secret（需要替换为自己的）
    private static final String ACCESS_KEY_SECRET = "your_access_key_secret";

	/**
	 * 发送短信
	 * @param signName 签名
	 * @param templateCode 模板
	 * @param phoneNumbers 手机号
	 * @param param 参数
	 */
	public static void sendMessage(String signName, String templateCode, String phoneNumbers, String param){
		DefaultProfile profile = DefaultProfile.getProfile(
		        "cn-hangzhou",
				"TA05tD92ymWvM9pgR9Y7W",
		        "bJskgR9jow7zZtSgj8mSq4pJUNkKo");
		IAcsClient client = new DefaultAcsClient(profile);

		SendSmsRequest request = new SendSmsRequest();
		request.setSysRegionId("cn-hangzhou");
		request.setPhoneNumbers(phoneNumbers);
		request.setSignName(signName);
		request.setTemplateCode(templateCode);
		request.setTemplateParam("{\"code\":\""+param+"\"}");
		
		try {
			SendSmsResponse response = client.getAcsResponse(request);
			log.info("短信发送成功，状态码: {}", response.getCode());
		} catch (ClientException e) {
			log.error("短信发送失败: {}", e.getMessage());
			e.printStackTrace();
		}
	}
}
