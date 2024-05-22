package com.cms.incidentmanagement.configuration;

import com.cms.incidentmanagement.utility.Constant;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class ExceptionConfig {

	public HashMap<String, Object> getTryCatchErrorMap(Exception e) {
		HashMap<String, Object> response = new HashMap<>();
		response.put(Constant.STATUS, Constant.ERROR);
		response.put(Constant.MESSAGE, new HashMap<String, String>() {{
			put(Constant.TEXT, Constant.TRY_AGAIN_LATER);
			put(Constant.REASON, e.getMessage());
		}});
		e.printStackTrace();
		return response;
	}
}
