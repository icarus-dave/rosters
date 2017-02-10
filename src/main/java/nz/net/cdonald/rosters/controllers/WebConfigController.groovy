package nz.net.cdonald.rosters.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.AbstractEnvironment
import org.springframework.core.env.Environment
import org.springframework.core.env.MapPropertySource
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/*
	Any config property prefixed with "webconfig." will be returned
	to the caller sans the prefix. The intention being to enable the front
	end to query the backend for environment variables (and easily cacheable).
 */

@RestController
@RequestMapping("/api/webconfig")
class WebConfigController {

	@Autowired
	Environment environment

	@RequestMapping
	public Map<String, String> config() {
		def webConfig = [:]
		(environment as AbstractEnvironment).getPropertySources()
				.findAll { it instanceof MapPropertySource && it.getSource() instanceof Map }
				.each { ps ->
			ps.getSource().each { k, v ->
				if (k.startsWith("frontend.")) webConfig.put(k - "frontend.", environment.getProperty(k) as String)
			}
		}
		return webConfig
	}

}
