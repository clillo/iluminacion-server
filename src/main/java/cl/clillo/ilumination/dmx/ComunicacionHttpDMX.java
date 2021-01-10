package cl.clillo.ilumination.dmx;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

@Component
public class ComunicacionHttpDMX {

	@Value("${dmx.http.url}")
    private String httpUrl;

	public void enviar(final int canal, final int dato){
		
		if (canal<0 || canal>512 || dato<0 || dato>255) {
			return;
		}
		
    	try {

  	      final StringBuilder result = new StringBuilder();
  	      final URL url = new URL(httpUrl + "dmx?canal="+String.valueOf(canal)+"&valor="+String.valueOf(dato));
  	    //  System.out.println(url);
  	      final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
  	      conn.setRequestMethod("GET");
  	      final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
  	      String line;
  	      while ((line = rd.readLine()) != null) {
  	         result.append(line);
  	      }
  	      rd.close();
  	 	      
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}	
}