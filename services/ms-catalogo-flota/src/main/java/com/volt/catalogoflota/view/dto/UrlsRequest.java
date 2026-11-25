package com.volt.catalogoflota.view.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record UrlsRequest(
		@NotEmpty List<String> urls) {

	public UrlsRequest {
		urls = List.copyOf(urls);
	}
}
