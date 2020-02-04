package com.strandls.esmodule.utils;

public enum Scripts {
	
	ActivityScore("params.content + params.participate"),
	ContentScore("Math.round(10*(Math.log10(params.content)))"),
	ParticipateScore("Math.round(10*(Math.log10(params.participate)))");
	
	private String script;
	
	private Scripts(String script) {
		this.script = script;
	}

	public String getScript() {
		return script;
	}
}
