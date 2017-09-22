package com.bqt.retrofit.bean;

import java.util.ArrayList;
import java.util.List;

public class RetrofitBean {

	private Integer total_count;
	private Boolean incompleteResults;
	private List<Item> items = new ArrayList<Item>();

	public Integer getTotalCount() {
		return total_count;
	}

	public void setTotalCount(Integer totalCount) {
		this.total_count = totalCount;
	}

	public Boolean getIncompleteResults() {
		return incompleteResults;
	}

	public void setIncompleteResults(Boolean incompleteResults) {
		this.incompleteResults = incompleteResults;
	}

	public List<Item> getItems() {
		return items;
	}
}
