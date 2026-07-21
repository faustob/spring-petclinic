/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.system;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;

/**
 * Receives Web Vitals measurements (LCP, INP) reported from the browser via the
 * web-vitals-report.js client script, and records them as OpenTelemetry histograms
 * using the global meter registered at application startup.
 */
@RestController
public class WebVitalsController {

	private final DoubleHistogram lcpHistogram;

	private final DoubleHistogram inpHistogram;

	public WebVitalsController() {
		Meter meter = GlobalOpenTelemetry.getMeter("org.springframework.samples.petclinic.web-vitals");
		this.lcpHistogram = meter.histogramBuilder("web.vital.lcp")
			.setDescription("Largest Contentful Paint reported from real user sessions")
			.setUnit("ms")
			.build();
		this.inpHistogram = meter.histogramBuilder("web.vital.inp")
			.setDescription("Interaction to Next Paint reported from real user sessions")
			.setUnit("ms")
			.build();
	}

	@PostMapping("/api/vitals")
	public ResponseEntity<Void> reportVital(@RequestBody VitalReport report) {
		if (report == null || report.name() == null) {
			return ResponseEntity.badRequest().build();
		}
		String route = report.route() == null ? "unknown" : report.route();
		Attributes attributes = Attributes.builder().put("http.route", route).build();
		if ("LCP".equals(report.name())) {
			this.lcpHistogram.record(report.value(), attributes);
		}
		else if ("INP".equals(report.name())) {
			this.inpHistogram.record(report.value(), attributes);
		}
		return ResponseEntity.accepted().build();
	}

	public record VitalReport(String name, double value, String id, String route) {

	}

}
