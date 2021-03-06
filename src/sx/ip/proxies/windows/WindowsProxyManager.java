/* 
 * Copyright 2018 IP Exchange : https://ip.sx/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sx.ip.proxies.windows;

import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellNotAvailableException;
import com.profesorfalken.jpowershell.PowerShellResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import sx.ip.proxies.ProxyManager;
import sx.ip.proxies.ProxySettings;

/**
 * Proxy Manager to be used when the OS is Windows.
 */
public class WindowsProxyManager extends ProxyManager {
    
    /** The logger Object.  */
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(WindowsProxyManager.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setProxySettings(ProxySettings settings) throws ProxySetupException {
        boolean commandResult = false;
        try {
            commandResult = setInternetProxy(settings);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new ProxySetupException(ex.getMessage(), ex);
            
        } catch (PowerShellNotAvailableException ex){
            LOGGER.error(ex.getMessage(), ex);
            throw  new ProxySetupException(ex.getMessage(), ex);
        }
        return commandResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProxySettings getProxySettings() throws ProxySetupException {
        try {
            return getInternetProxy();
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new ProxySetupException(ex.getMessage(), ex);
        } catch (PowerShellNotAvailableException ex){
            LOGGER.error(ex.getMessage(), ex);
            throw  new ProxySetupException(ex.getMessage(), ex);
        }
    }

    /**
     * Main function for tests.
     * 
     * @param args 
     *          the command line arguments
     * @throws java.io.IOException When a IOException happen
     */
    public static void main(String[] args) throws IOException {
        ProxySettings settings = new ProxySettings("127.0.0.1", 8080, ProxySettings.ProxyType.HTTP, "http://proxy:7892", true, "user", "pass");
        
        setInternetProxy(settings);
        getInternetProxy();
        disableInternetProxy();
        
    }

    /**
     * Method resposible for start the Proxy connection.
     *
     * @param settings 
     *          A ProxySettings instance that hold all necessary configurations
     * @throws IOException The IO exception.
     * 
     * @return If the command was executed or not.
     */
    public static boolean setInternetProxy(ProxySettings settings) throws IOException,PowerShellNotAvailableException  {        
        String result = "false";
        String powerShellScript = IOUtils.resourceToString("/sx/ip/proxies/windows/scripts/windows-set-internet-proxy.ps1", Charset.forName("UTF-8"));
        if(settings.getProxyHost() != null && settings.getProxyHost().length() > 0){
            LOGGER.info("Starting setInternetProxy....");
            switch (settings.getType()) {
                case HTTP:
                    result = runPowerShellMethod(powerShellScript, "http", settings, false, false, null, false).get("result");
                    break;
                case HTTPS:
                    result = runPowerShellMethod(powerShellScript, "https", settings, false, false, null, false).get("result");
                    break;
                case SOCKS:
                    result = runPowerShellMethod(powerShellScript, "socks", settings, false, false, null, false).get("result");
                    break;
                case FTP:
                    result = runPowerShellMethod(powerShellScript, "ftp", settings, false, false, null, false).get("result");
                    break;
                case HTTP_AND_HTTPS:
                    result = runPowerShellMethod(powerShellScript, "http_https", settings, false, false, null, false).get("result");
                    
                    break;
            }
            LOGGER.info("Ending setInternetProxy....");
        }else if(settings.getAcsUrl() != null){
            result = runPowerShellMethod(powerShellScript, "", settings, false, false, null, false).get("result");
        }else{
            result = disableInternetProxy();
            
        }
        
        return "true".equals(result);
    }

    /**
     * Method resposible for get the Proxy connection.
     *
     * @throws IOException The IO exception.
     * 
     * @return The latest ProxySettings.
     *
     */
    public static ProxySettings getInternetProxy() throws IOException, PowerShellNotAvailableException {
        LOGGER.info("Starting getInternetProxy....");
        String powerShellScript = IOUtils.resourceToString("/sx/ip/proxies/windows/scripts/windows-get-internet-proxy.ps1", Charset.forName("UTF-8"));
        String proxyEnable = runPowerShellMethod(powerShellScript, null, null, false, true, "ProxyEnable", false).get("output").trim();
        ProxySettings settings = null;

        if ((proxyEnable != null) && ("1".equals(proxyEnable))) {
            String[] output = runPowerShellMethod(powerShellScript, null, null, false, true, "ProxyServer", false).get("output").trim().split("=");
            String bypass = runPowerShellMethod(powerShellScript, null, null, false, true, "ProxyOverride", false).get("output");
            String acs = runPowerShellMethod(powerShellScript, null, null, false, true, "AutoConfigURL", false).get("output");
            String[] credentials = runPowerShellMethod(powerShellScript, null, null, false, true, null, true).get("output").split(":");
            boolean hasByPass = !bypass.trim().isEmpty();
            String[] server = output[1].split(":");

            settings = new ProxySettings(server[0], Integer.valueOf(server[1]), ProxySettings.ProxyType.valueOf(output[0].toUpperCase()),
                                         acs.trim(), hasByPass, credentials[0], credentials[1].trim());
        }
        LOGGER.info("Ending getInternetProxy....");
        return settings;
    }

    /**
     * Method resposible for disable the Proxy connection.
     *
     * @throws IOException The IO exception.
     * 
     * @return If the command was executed or not.
     *
     */
    public static String disableInternetProxy() throws IOException, PowerShellNotAvailableException {
        LOGGER.info("Starting disableInternetProxy....");
        String powerShellScript = IOUtils.resourceToString("/sx/ip/proxies/windows/scripts/windows-disable-internet-proxy.ps1", Charset.forName("UTF-8"));
        String result = runPowerShellMethod(powerShellScript, null, null, true, false, null, false).get("result");
        LOGGER.info("Ending disableInternetProxy....");
        return result;
    }

    /**
     * Function responsible for execute the calls to the PowerShell Script
     * Methods.
     *
     * @param powerShellScript 
     *              The power shell script for proxy setting
     * @param proxyType 
     *              Type of proxy, what can be (http, https, socks and ftp)
     * @param settings 
     *              ProxySettings instance containing all proxy configurations
     * @param disable 
     *              Flag to indicate if is to use the disable script
     * @param getScript 
     *              Flag to indicate if is to use the get script
     * @param settingName 
     *              Name of the setting to get
     * @param getCredentials 
     *              Flag to indicate if is to get the credentials of the proxy
     *
     * @return A map result and output message.
     * 
     * @throws PowerShellNotAvailableException The PowerShell Exception.
     */
    private static Map<String, String> runPowerShellMethod(String powerShellScript, String proxyType, ProxySettings settings, 
                                                           boolean disable, boolean getScript, String settingName, boolean getCredentials) throws PowerShellNotAvailableException{
        System.out.println("Starting runPowerShellMethod....");
        LOGGER.info("Starting runPowerShellMethod....");
        Map<String, String> response = new HashMap<>();
        String psMethod;
        PowerShell powerShell = null;

        if (disable) {
            psMethod = "Disable-InternetProxy";
        } else if (getScript) {
            if (!getCredentials) {
                psMethod = "Get-InternetProxy -name " + settingName;
            } else {
                psMethod = "Get-InternetProxy";
            }
        } else {
            if("http_https".equals(proxyType)){
                psMethod = "Set-InternetProxy -proxy \"http=" 
                        + settings.getProxyHost() + ":" + settings.getProxyPort()
                        +";https=" + settings.getProxyHost() + ":" + settings.getProxyPort() 
                        + "\" "
                        + (settings.getAcsUrl() != null && !settings.getAcsUrl().isEmpty() ? "-acs \"" + settings.getAcsUrl() + "\"" : "")
                        + " "
                        + (settings.getAuthUser() != null && !settings.getAuthUser().isEmpty() ? "-authuser \"" + settings.getAuthUser() + "\"" : "")
                        + " "
                        + (settings.getAuthPass() != null && !settings.getAuthPass().isEmpty() ? "-authpass \"" + settings.getAuthPass() + "\"" : "")
                        + " "
                        + (settings.getBypassOnLocal() ? "-bypass \"true\"" : "")
                        + " "
                        + (settings.getProxyHost() != null && !settings.getProxyHost().isEmpty() ? "-hostname \"" + settings.getProxyHost() + "\"" : "");
            }else{
                psMethod = "Set-InternetProxy" 
                        + (proxyType != null && !proxyType.isEmpty() ? " -proxy \"" + proxyType + "=" : "") 
                        + (settings.getProxyHost() != null && !settings.getProxyHost().isEmpty() ? settings.getProxyHost() + ":" : "") 
                        + (settings.getProxyPort() != null ? String.valueOf(settings.getProxyPort()) + "\"" : "")
                        + (settings.getAcsUrl() != null && !settings.getAcsUrl().isEmpty() ? " -acs \"" + settings.getAcsUrl() + "\"" : "")
                        + " "
                        + (settings.getAuthUser() != null && !settings.getAuthUser().isEmpty() ? "-authuser \"" + settings.getAuthUser() + "\"" : "")
                        + " "
                        + (settings.getAuthPass() != null && !settings.getAuthPass().isEmpty() ? "-authpass \"" + settings.getAuthPass() + "\"" : "")
                        + " "
                        + (settings.getBypassOnLocal() ? "-bypass \"true\"" : "")
                        + " "
                        + (settings.getProxyHost() != null && !settings.getProxyHost().isEmpty() ? "-hostname \"" + settings.getProxyHost() + "\"" : "");
            }
        }

        try {
            
            powerShell = PowerShell.openSession();
            powerShell.executeCommand(powerShellScript);

            PowerShellResponse psResponse = powerShell.executeCommand(psMethod);

            if (psResponse.isError()) {
                response.put("result", "false");
            } else {
                response.put("result", "true");
            }
            
            LOGGER.info(psResponse.getCommandOutput());
            System.out.println("Output:" + psResponse.getCommandOutput());
            response.put("output", psResponse.getCommandOutput());
        } catch (PowerShellNotAvailableException ex) {
            throw ex;
        } finally {
            LOGGER.info("Ending runPowerShellMethod....");
            System.out.println("Ending runPowerShellMethod....");

            if (powerShell != null) {
                powerShell.close();
            }
        }

        return response;
    }

}
