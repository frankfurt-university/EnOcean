package de.EnOcean.jslee.ratype;

public interface EnOceanConnectionActivity {
	public EnOceanConnectionActivity getEnOceanActivity();
	public void checkReadyStatus();
	public void sendEnOceanTelegram(String telegram);
}
