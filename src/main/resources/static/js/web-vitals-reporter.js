/*
 * Client-side Web Vitals reporter for Spring PetClinic.
 * Captures LCP (Largest Contentful Paint) and INP (Interaction to Next Paint)
 * using the web-vitals library (loaded from CDN below) and POSTs them to a
 * server endpoint which records them as OpenTelemetry metrics.
 *
 * This file holds ZERO OpenTelemetry imports/APIs — it only collects
 * measurements in the browser and reports them to the server.
 */
(function () {
	function sendToServer(metric) {
		try {
			var payload = JSON.stringify({
				name: metric.name,
				value: metric.value,
				id: metric.id,
				route: window.location.pathname
			});
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
			// Never let telemetry reporting break the page.
		}
	}

	var script = document.createElement('script');
	script.type = 'module';
	script.textContent = "import {onLCP, onINP} from 'https://unpkg.com/web-vitals@4/dist/web-vitals.js';" +
		"onLCP(window.__petclinicReportVital); onINP(window.__petclinicReportVital);";
	window.__petclinicReportVital = sendToServer;
	document.head.appendChild(script);
})();
