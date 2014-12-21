/*******************************************************************************
 * Copyright (c) 2014 SAP AG or an SAP affiliate company. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *******************************************************************************/

package com.sap.dirigible.runtime.flow;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.sap.dirigible.repository.api.IRepository;
import com.sap.dirigible.runtime.command.CommandExecutor;
import com.sap.dirigible.runtime.command.CommandServlet;
import com.sap.dirigible.runtime.js.JavaScriptExecutor;
import com.sap.dirigible.runtime.js.JavaScriptServlet;
import com.sap.dirigible.runtime.logger.Logger;
import com.sap.dirigible.runtime.scripting.AbstractScriptExecutor;

public class FlowExecutor extends AbstractScriptExecutor {
	
	private static final Logger logger = Logger.getLogger(FlowExecutor.class);

	private IRepository repository;
	private String[] rootPaths;
	
	private Gson gson = new Gson();

	public FlowExecutor(IRepository repository, String... rootPaths) {
		super();
		logger.debug("entering: constructor()");
		this.repository = repository;
		this.rootPaths = rootPaths;
		if (this.rootPaths == null || this.rootPaths.length == 0) {
			this.rootPaths = new String[] { null, null };
		}
		logger.debug("exiting: constructor()");
	}

	@Override
	public Object executeServiceModule(HttpServletRequest request, HttpServletResponse response,
			Object input, String module, Map<Object, Object> executionContext) throws IOException {

		logger.debug("entering: executeServiceModule()"); //$NON-NLS-1$
		logger.debug("module=" + module); //$NON-NLS-1$
		
		if (module == null) {
			throw new IOException("Flow module cannot be null");
		}
		
		String result = null; 
		String flowSource = new String(retrieveModule(repository, module, "", rootPaths).getContent());
		
		Flow flow = gson.fromJson(flowSource, Flow.class);
		
		Object inputOutput = null;
		
		inputOutput = processFlow(request, response, module, executionContext,
				flow, inputOutput);

		result = (inputOutput != null) ? inputOutput.toString() : "";
		
		logger.debug("exiting: executeServiceModule()");
		return result;
	}

	private Object processFlow(HttpServletRequest request,
			HttpServletResponse response, String module,
			Map<Object, Object> executionContext, Flow flow, Object inputOutput)
			throws IOException {
		executionContext.putAll(flow.getProperties());
		
		// TODO make extension point
		for (FlowStep flowStep : flow.getSteps()) {
			try {
				if (FlowStep.FLOW_STEP_TYPE_JAVASCRIPT.equalsIgnoreCase(flowStep.getType())) {
					JavaScriptServlet javaScriptServlet = new JavaScriptServlet();
					JavaScriptExecutor javaScriptExecutor = javaScriptServlet.createExecutor(request);
					inputOutput = javaScriptExecutor.executeServiceModule(request, response, inputOutput, flowStep.getModule(), executionContext);
//				} else if (FlowStep.FLOW_STEP_TYPE_JAVA.equalsIgnoreCase(flowStep.getType())) {
//					JavaServlet javaServlet = new JavaServlet();
//					JavaExecutor javaExecutor = javaServlet.createExecutor(request);
//					inputOutput = javaExecutor.executeServiceModule(request, response, inputOutput, module, executionContext);
				} else if (FlowStep.FLOW_STEP_TYPE_COMMAND.equalsIgnoreCase(flowStep.getType())) {
					CommandServlet commandServlet = new CommandServlet();
					CommandExecutor commandExecutor = commandServlet.createExecutor(request);
					inputOutput = commandExecutor.executeServiceModule(request, response, inputOutput, flowStep.getModule(), executionContext);
				} else if (FlowStep.FLOW_STEP_TYPE_CONDITION.equalsIgnoreCase(flowStep.getType())) {
					
					FlowCase[] cases = flowStep.getCases();
					for (FlowCase flowCase : cases) {
						Object value = executionContext.get(flowCase.getKey());
						if (value != null
								&& value.equals(flowCase.getValue())) {
							processFlow(request, response, module, executionContext, flowCase.getFlow(), inputOutput);
							break;
						}
					}
					
				} else { // groovy etc...
					throw new IllegalArgumentException(String.format("Unknown execution type [%s] of step %s in flow %s at %s", 
							flowStep.getType(), flowStep.getName(), flow.getName(), module));
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			}
		}
		return inputOutput;
	}

	@Override
	protected void registerDefaultVariable(Object scope, String name,
			Object value) {
		// do nothing
	}

}
