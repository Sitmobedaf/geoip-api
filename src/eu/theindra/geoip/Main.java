package eu.theindra.geoip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import com.maxmind.geoip.LookupService;

public class Main extends JavaPlugin {

	public static Main instance;
	public static LookupService geoip = null;

	public void onEnable() {
		instance = this;

		Bukkit.getConsoleSender().sendMessage("This API includes GeoLite data created by MaxMind, available from http://www.maxmind.com");
		File base = new File(getDataFolder(), "GeoLiteCity.dat");
		boolean mustDownload = false;
		if (!base.exists()) {
			mustDownload = true;
		} else {
			if (System.currentTimeMillis() - base.lastModified() > 86400000) {
				mustDownload = true;
			}
		}
		if (mustDownload) {
			try {
				downloadFrom("http://geolite.maxmind.com/download/geoip/database/GeoLiteCity.dat.gz");
			} catch (IOException e) {
				Bukkit.getConsoleSender().sendMessage("An error occurred while trying to load the database: " + e.getMessage());
			}
		} else {
			Bukkit.getLogger().info("The database was not updated, since the last update was made less than a day ago.");
		}
		try {
			geoip = new LookupService(base, LookupService.GEOIP_CHECK_CACHE);
		} catch (IOException e) {
			Bukkit.getConsoleSender().sendMessage("Something went wrong.");
			Bukkit.getPluginManager().disablePlugin(this);
		}
	}

	public void onDisable() {
		geoip.close();
	}

	public void downloadFrom(String url) throws IOException {
		URL CityURL = new URL(url);

		URLConnection connection = CityURL.openConnection();
		connection.setConnectTimeout(5000);
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Minecraft server) Bukkit");
		connection.connect();
		InputStream input = connection.getInputStream();
		input = new GZIPInputStream(input);

		// idk why i need this but it throwed an expection
		getDataFolder().mkdir();

		OutputStream output = new FileOutputStream(new File(getDataFolder(), "GeoLiteCity.dat"));
		byte[] buffer = new byte[2048];
		int length = input.read(buffer);
		while (length >= 0) {
			output.write(buffer, 0, length);
			length = input.read(buffer);
		}

		// we don't want to leak memory right?
		output.close();
		input.close();
	}
}
