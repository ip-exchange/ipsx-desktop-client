####
# SET-INTERNETPROXY
#
# DESCRIPTION
#   This function will set the proxy server and (optional) Automatic configuration script.
#
# SYNTAX
#   Set-InternetProxy [-Proxy] <string[]> [[-acs] <string[]>]  [<CommonParameters>]
#
# EXAMPLES 
#   Setting proxy information: 
#        Set-InternetProxy -proxy "127.0.0.1:8080" 
#   Setting a socks proxy: 
#        Set-InternetProxy -proxy "socks=127.0.0.1:8080" 
#   Setting proxy information and (optinal) Automatic Configuration Script:
#       Set-InternetProxy -proxy "proxy:7890" -acs "http://proxy:7892"
#
#   Setting proxy information, (optinal) Automatic Configuration and (optinal) Authentication Script:
#       Set-InternetProxy -proxy "proxy:7890" -acs "http://proxy:7892" -authuser "username" -authpass "password" -bypass -host "hostname"
#
# SOURCE
#   https://gallery.technet.microsoft.com/scriptcenter/PowerShell-function-Get-cba2abf5
####

Function Set-InternetProxy
{
    [CmdletBinding()]
    Param(        
        [Parameter(Mandatory=$False,ValueFromPipeline=$true,ValueFromPipelineByPropertyName=$true)]
        [String]$proxy,

        [Parameter(Mandatory=$False,ValueFromPipeline=$true,ValueFromPipelineByPropertyName=$true)]
        [AllowEmptyString()]
        [String[]]$acs,

        [Parameter(Mandatory=$False,ValueFromPipeline=$true,ValueFromPipelineByPropertyName=$true)]
        [AllowEmptyString()]
        [String]$authuser,

        [Parameter(Mandatory=$False,ValueFromPipeline=$true,ValueFromPipelineByPropertyName=$true)]
        [AllowEmptyString()]
        [String]$authpass,

        [Parameter(Mandatory=$False,ValueFromPipeline=$true,ValueFromPipelineByPropertyName=$true)]
        [AllowEmptyString()]
        [String]$bypass,

        [Parameter(Mandatory=$False,ValueFromPipeline=$true,ValueFromPipelineByPropertyName=$true)]
        [AllowEmptyString()]
        [String]$hostname
    )

    Begin
    {
            $regKey="HKCU:\Software\Microsoft\Windows\CurrentVersion\Internet Settings"        
    }
    
    Process
    {                    
        if($acs) 
        {                        
            Set-ItemProperty -path $regKey AutoConfigURL -Value $acs          
        }
        else
        {
            Set-ItemProperty -path $regKey ProxyEnable -value 1
            Set-ItemProperty -path $regKey ProxyServer -value $Proxy 
            
            if($bypass)
            {
                Set-ItemProperty -path $regKey ProxyOverride -value "<local>"
            }
        }

        if($authuser)
        {
            cmdkey /generic:$hostname /user:$authuser /pass:$authpass 
        }
    } 
    
    End
    {
        Write-Output "Proxy is now enabled"
        if($proxy)
        {
            Write-Output "Proxy Server : $proxy"
        }
        if($authuser)
        {
            Write-Output "Proxy User : $authuser"
        }
        if ($acs)
        {            
            Write-Output "Automatic Configuration Script : $acs"
        }
        else
        {            
            Write-Output "Automatic Configuration Script : Not Defined"
        }
        if ($bypass)
        {            
            Write-Output "Bypass on local enabled"
        }
        else
        {            
            Write-Output "Bypass on local disabled"
        }
    }
}

# Keep this line and make sure there is an empty line below this one

