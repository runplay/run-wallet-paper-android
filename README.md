# run IOTA paper
![alt tag](http://iota.runplay.com/img/paper-logo.png "run IOTA wallet Logo")
  

```

WARNING !
This repository is now out of date a no longer worked on, only use for reference, an update to the latest IOTA library is required for it to work

```

<b>run Iota paper</b> provides a Paper wallet and digital encrypted Hybrid Master Slave wallets for IOTA
<br/><br/>
<b>Html encrypted digital wallets</b><br/><br/>
The Master Digital wallet simply provides access to the Seed, encrypted with SHA-256 and compiled to a self contained html file, run anywhere.
<br/><br/>
The Slave / View wallet provides ledger access to view the Addresses on the seed, without the need for the Seed, encrypted with SHA-256, attach them and start receiving IOTA.
<br/><br/>
A simple use case scenario: Boss generates Master and Slave wallets, keeps master (Seed) and gives Slave (Viewer) wallets to Accounts and Sales departments safe in the knowledge that the Seed is never given out, but both departments get full view access to the transactions.
<br/><br/>
There are no spend functions with run IOTA paper, it is designed to only receive IOTA payments, with separation between Seed owner and people (staff) with permission to view the transfer on the Seed.
<br/><br/>
<h2>Features</h2>

Full of features<br/>
✅ Safely generate a seed or import one<br/>
✅ <b>Print a Paper wallet</b> 🌿<br/>
✅ <b>Create a Digital Paper Master wallet</b> 🌿<br/>
✅  <b>Create a Digital Paper View wallet</b> 🌿<br/>
✅ SHA-PBKDF2 level encryption<br/>
✅ Pre generate as many IOTA Addresses to a Seed as wanted<br/>
✅ Secure Delete any files created, cannot be recovered after<br/>

<br/><br/>
https://github.com/iotaledger/android-wallet-paper-android
<br/>
<br/>
<h2>Updates</h2>
v1.2.030 - Add Trinity wallet compatibility on QR, bug fix on Files area app crash<br/>
v1.1.030 - Improved View wallet speed, display data and copy data, Added wallet view and share button to files section, Other misc. fixes and tweaks<br/>
v1.1.021 - First release<br/>
<br/><br/>

<br/><br/>
<h2>Download</h2>

<a href="https://play.google.com/store/apps/details?id=run.wallet.paper"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" width="323" height="125"/></a>

<h2>Structure</h2>
The app has been kept to a minimalist structure to enable ease of Audit.<br/>
<br/>
```
|app
   |src
       |main
           |assets
           |    |addresses.html (View wallet html template)
           |    |paper.html (Seed wallet html template)
           |java
           |    |jota (stripped-down, internet free IOTA jota library for generating addresses, no transaction abilities)
           |    |run
           |         |wallet
           |              |common/delete (Secure delete file classes)
           |              |paper
           |                   |AppService.java (Android Service, used for Address generation and Delete files)
           |                   |Main.java (Everything to make the app work, kept in one class for ease of Audit)
           |res (App resource files)
           |AndroidMainfest.xml
```

<h2>How to build</h2>

```bash
$ git clone https://github.com/runplay/run-wallet-paper-android
$ cd run-wallet-paper-android
$ ./gradlew clean build
```

Available only on Github, Google Play and Amazon market, do not download run IOTA paper anywhere else


<h2>Support the project</h2>
If you find the app useful, donations are accepted here

IOTA: 9PPMLVNEGQEZLCKTPDSCMKNKNDPMHUTC9PMOAKHGNGOVVTXOTRA99JFPVAAXHPUM99DGLUHOYLMWOL9YCSGRZJIYSW



<h2>Screens</h2>
Here are some screenshots of the app

![alt tag](http://iota.runplay.com/img/gp-aws1.png "Android Screens")

![alt tag](http://iota.runplay.com/img/gp-aws2.png "Android Screens")

![alt tag](http://iota.runplay.com/img/gp-aws4.png "Html wallet screens")

![alt tag](http://iota.runplay.com/img/gp-aws3.png "Android Screens")

