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
 * Receives Web Vitals measurements (LCP, INP) reported from the browser via
 * the web-vitals-reporter.js client script, and records them as
 * OpenTelemetry metrics using the server's global meter provider.
 */
@RestController
class WebVitalsController {

	private static final Meter METER = GlobalOpenTelemetry.getMeter("org.springframework.samples.petclinic.web-vitals");

	private static final DoubleHistogram LCP_HISTOGRAM = METER.histogramBuilder("web.vital.lcp")
		.setUnit("ms")
		.setDescription("Largest Contentful Paint reported from real user browsers")
		.build();

	private static final DoubleHistogram INP_HISTOGRAM = METER.histogramBuilder("web.vital.inp")
		.setUnit("ms")
		.setDescription("Interaction to Next Paint reported from real user browsers")
		.build();

	@PostMapping("/api/vitals")
	public ResponseEntity<Void> reportVital(@RequestBody VitalPayload payload) {
		if (payload != null && payload.name() != null && payload.value() != null) {
			String route = payload.route() != null ? payload.route() : "unknown";
			Attributes attributes = Attributes.builder().put("http.route", route).build();
			if ("LCP".equalsIgnoreCase(payload.name())) {
				LCP_HISTOGRAM.record(payload.value(), attributes);
			}
			else if ("INP".equalsIgnoreCase(payload.name())) {
				INP_HISTOGRAM.record(payload.value(), attributes);
			}
		}
		return ResponseEntity.accepted().build();
	}

	record VitalPayload(String name, Double value, String id, String route) {
	}

}
