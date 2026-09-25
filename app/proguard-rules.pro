# Keep line numbers in crash stack traces, but hide the original file names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Gson maps JSON to these classes by field name via reflection, so R8 must keep
# their fields: the openFDA and Gemini network models, and the entities the
# pre-Room data migration reads from SharedPreferences JSON.
-keep class com.qihang.medtrack.data.drug.DrugLabelResponse { *; }
-keep class com.qihang.medtrack.data.drug.DrugLabelResult { *; }
-keep class com.qihang.medtrack.data.drug.OpenFda { *; }
-keep class com.qihang.medtrack.data.genai.Gemini* { *; }
-keep class com.qihang.medtrack.data.patient.Patient { *; }
-keep class com.qihang.medtrack.data.medication.Medication { *; }

# Gson reads generic type information (e.g. List<Patient>) from TypeToken subclasses.
-keepattributes Signature
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
