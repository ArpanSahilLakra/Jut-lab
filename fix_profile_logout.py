import re

with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'r') as f:
    text = f.read()

# add imports if needed
if 'import com.example.repository.AuthSessionManager' not in text:
    text = text.replace('import androidx.compose.ui.unit.sp', 'import androidx.compose.ui.unit.sp\nimport com.example.repository.AuthSessionManager\nimport androidx.compose.ui.platform.LocalContext\nimport com.example.data.LabDatabase\nimport androidx.compose.ui.graphics.Color\nimport com.example.ui.theme.DangerRed')

# add logout button
old_button = 'NeoButton(text = "Back", buttonType = NeoButtonType.BACK, onClick = onBack, modifier = Modifier.fillMaxWidth())'
new_button = '''
      val context = LocalContext.current
      val scope = rememberCoroutineScope()
      val labDao = remember { LabDatabase.getDatabase(context).labDao() }
      
      NeoButton(
          text = "LOGOUT",
          onClick = {
              AuthSessionManager.explicitLogout(scope, labDao, onComplete = {})
          },
          backgroundColor = DangerRed,
          textColor = Color.White,
          modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(12.dp))
      NeoButton(text = "Back", buttonType = NeoButtonType.BACK, onClick = onBack, modifier = Modifier.fillMaxWidth())
'''

if 'text = "LOGOUT"' not in text:
    text = text.replace(old_button, new_button)

with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'w') as f:
    f.write(text)

