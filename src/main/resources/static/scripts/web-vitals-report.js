/*
 * Client-side Web Vitals reporter.
 * Captures LCP and INP using the web-vitals library (loaded via CDN in the page templates)
 * and POSTs them to the server-side /api/vitals endpoint, which records them through the
 * OpenTelemetry SDK registered in PetClinicApplication. This file holds no OTel imports —
 * all metric recording happens server-side.
 */
(function () {
	if (typeof window === 'undefined') {
		return;
	}

	function sendToServer(metric) {
		var payload = JSON.stringify({
			name: metric.name,
			value: metric.value,
			id: metric.id,
			route: window.location.pathname
		});
		try {
			if (navigator.sendBeacon) {
				var blob = new Blob([payload], { type: 'application/json' });
				navigator.sendBeacon('/api/vitals', blob);
			}
			else {
				fetch('/api/vitals', {
					method: 'POST',
					headers: { 'Content-Type': 'application/json' },
					body: payload,
					keepalive: true
				});
			}
		}
		catch (e) {
			// best-effort reporting only
		}
	}

	if (window.webVitals) {
		window.webVitals.onLCP(sendToServer);
		window.webVitals.onINP(sendToServer);
	}
})();
