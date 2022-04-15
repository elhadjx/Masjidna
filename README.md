# Masjidna 

[![Masjid,a](https://i.ibb.co/bzRWk5m/icon.png)](https://github.com/dinoelhadj/Masjidna)




Masjidna is an Android App for a predefined custom masjid where you can see and get prayer times and adhan notifications, the donated amount by the prayers to that specific masjid over time and lastly get news or messages about anything important to know from the masjid, It is both available in Arabic and Spanish.

This app has a simple admin app that sets custom prayer times and inserts the rest of the data needed by this app.

# Screenshots
<img src="Screenshots/Screenshot (1).jpg" alt="phone image" width="200px" />
<img src="Screenshots/Screenshot (2).jpg" alt="phone image" width="200px" /><img src="Screenshots/Screenshot (3).jpg" alt="phone image" width="200px" /><img src="Screenshots/Screenshot (4).jpg" alt="phone image" width="200px" /><img src="Screenshots/Screenshot (5).jpg" alt="phone image" width="200px" /><img src="Screenshots/Screenshot (6).jpg" alt="phone image" width="200px" />



# What does this app do?
It is an open source Android application that allows Muslims to keep track of prayer times of their preferred masjid. They also can keep track of Sadaka that they donate to the masjid every Jumua. Finally they can get notifications and messages about any news or important information they should know, The App has a simple admin app that sets custom prayer times and inserts the rest of the data needed by this app.



# Libraries this app uses:

1. Firebase - https://firebase.google.com/ 

# Setup

1. Clone or download this repo.
2. This app runs off a Firebase backend. You will need to generate your firebase backend. To do this, navigate to https://firebase.google.com/ and sign up. Create a project called 
"Masjidna" (or what ever you want to call it). 
3. Navigate to "Database", select the three dots and then select "Import JSON". Select the file [Server/masjidna-default-rtdb.json](Server/masjidna-default-rtdb.json). 
4. You are free how to set your firebase rules
5. You will need to generate your own google-services.json file to use with the app. Navigate to the Firebase project settings dashboard. You should see an option to download the 
google-services.json file. This must then be placed into the app folder of this project. 


# [OPTIONAL setup] 
1. If you wish to build a release version you will need to create your own keystore file and edit the password values in the following file - (create a version of the file without the .sample extension): release-keystore.properties.sample
2. Setup a Fabric Account. https://fabric.io/dashboard
3. Get your Fabric API Key and Client key, change it in the file: /app/fabric-sample.properties and rename the file to fabric.properties

# Contributions
Contributions are welcome. 

# License
Copyright 2016 Masjidna.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements. See the NOTICE file distributed with this work for
additional information regarding copyright ownership. The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
