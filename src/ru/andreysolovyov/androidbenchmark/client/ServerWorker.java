package ru.andreysolovyov.androidbenchmark.client;

import java.util.ArrayList;
import java.util.Iterator;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import ru.andreysolovyov.androidbenchmark.R;
import android.content.Context;
import android.util.Log;

public class ServerWorker {
	public ServerWorker(Context ctx){
		this.ctx = ctx;
	}
	
	Context ctx;
	private static final String NAMESPACE = "http://benchmark.android.andreysolovyov.ru/";

	// Это то, что написано в SOAP Action потра в виздэле
	private static String URL = "http://192.168.150.1:8080/ws/BenchmarkImpl"; // "http://127.0.0.1:8080/ws/BenchmarkImpl";//
	private static String OPERATION = "submitResults";
	private static String ACTION = NAMESPACE + OPERATION;

	public ArrayList<BenchmarkResults> submitResults(
			BenchmarkResults thisDeviceResults) {
		ArrayList<BenchmarkResults> nearestResults = null;

		SoapObject request = new SoapObject(NAMESPACE, OPERATION);

		SoapObject newResultsRequest = new SoapObject("", "newResults");
		newResultsRequest.addProperty("model", thisDeviceResults.model);
		newResultsRequest.addProperty("intOps", thisDeviceResults.intOps);
		newResultsRequest.addProperty("floatOps", thisDeviceResults.floatOps);
		newResultsRequest.addProperty("doubleOps", thisDeviceResults.doubleOps);
		newResultsRequest.addProperty("overallMark",
				thisDeviceResults.overallMark);

		request.addSoapObject(newResultsRequest);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = false;
		envelope.setOutputSoapObject(request);

		HttpTransportSE androidHTTPTransport = new HttpTransportSE(URL);

		try {
			androidHTTPTransport.call(ACTION, envelope);
			if (envelope.bodyIn instanceof SoapObject) {
				nearestResults = new ArrayList<BenchmarkResults>();
				SoapObject soapObject = (SoapObject) envelope.bodyIn;
				if (soapObject != null) {
					for (int i = 0; i < soapObject.getPropertyCount(); i++) {
						SoapObject resultsNode = (SoapObject) soapObject
								.getProperty(i);
						BenchmarkResults br = new BenchmarkResults(
								resultsNode
										.getPrimitivePropertySafelyAsString("model"),
								Integer.parseInt((String) resultsNode
										.getPrimitivePropertySafely("intOps")),
								Integer.parseInt((String) resultsNode
										.getPrimitivePropertySafely("floatOps")),
								Integer.parseInt((String) resultsNode
										.getPrimitivePropertySafely("doubleOps")),
								Integer.parseInt((String) resultsNode
										.getPrimitivePropertySafely("overallMark")));
						nearestResults.add(br);
					}
					Iterator<BenchmarkResults> iterator = nearestResults
							.iterator();
					int i = 0;
					while (iterator.hasNext()) {
						Log.d("Benchmark", "HERE");
						BenchmarkResults current = iterator.next();

						BenchmarkResults thisDeviceTmpResults = new BenchmarkResults(
								ctx.getResources().getString(R.string.your_device), thisDeviceResults.intOps,
								thisDeviceResults.floatOps,
								thisDeviceResults.doubleOps,
								thisDeviceResults.overallMark);

						if (thisDeviceResults.overallMark > current.overallMark) {
							nearestResults.add(i, thisDeviceTmpResults);
							break;
						}
						i++;
					}
				}
			} else if (envelope.bodyIn instanceof SoapFault) {

				SoapFault soapFault = (SoapFault) envelope.bodyIn;
				Log.e("Benchmark ", soapFault.getMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nearestResults;
	}

	public void setServerInfo(String newURL, String newOperation) {
		if (newURL != null && newURL.length() > 0) {
			URL = newURL;
		}
		if (newOperation != null && newOperation.length() > 0) {
			OPERATION = newOperation;
		}
	}

	public String getURL() {
		return URL;
	}

	public String getOperation() {
		return OPERATION;
	}
}