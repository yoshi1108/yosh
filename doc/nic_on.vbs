Const ssfCONTROLS = 3 
Const sConPaneName = "�l�b�g���[�N�ڑ�" 
Const sConnectionName = "���[�J�� �G���A�ڑ� 4" 
Const sDisableVerb = "�����ɂ���(&B)" 
Const sEnableVerb = "�L���ɂ���(&A)" 

set shellApp = createobject("shell.application") 
set oControlPanel = shellApp.Namespace(ssfCONTROLS) 
set oNetConnections = nothing 

for each folderitem in oControlPanel.items 
	if folderitem.name = sConPaneName then 
		set oNetConnections = folderitem.getfolder: exit for 
	end if 
next 

if oNetConnections is nothing then 
	wscript.quit 
end if 

set oLanConnection = nothing 
for each folderitem in oNetConnections.items 
	if lcase(folderitem.name) = lcase(sConnectionName) then 
		set oLanConnection = folderitem: exit for 
	end if 
next 

if oLanConnection is nothing then 
	wscript.quit 
end if 

for each verb in oLanConnection.verbs 
	if verb.name = sEnableVerb then 
		verb.Doit 
		WScript.Sleep 2000 
	end if 
next 
