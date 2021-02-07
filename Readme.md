<h1>CS:GO case stats viewer</h1>

<h2>How does it work?</h2>
This Java Application loads your whole CS:GO <a href=https://steamcommunity.com/my/inventoryhistory>inventory history</a> by emulation the view more button.
To avoid rate limiting it loads 50 transactions per ~2.5 seconds. Which mean if you have a large history it will take longer to load.
After the inventory history is dumped it will search for every unboxed case.

<h2>Usage</h2>

1. Download the latest compiled version via the <a href=https://github.com/cantryDev/CSGOCaseStatsViewer/files/5939123/CSGOCaseStatsViewer-Release1.0.0.zip>latest release</a> or compile it yourself with maven.
2. Execute the execute.bat 
3. Follow the steps in the commandline which just got opened
4. Get disappointed.

<h2>Example result</h2>

<a href=https://github.com/cantryDev/CSGOCaseStatsViewer/blob/master/result_07_02_2021_14_54.txt>click</a>

<h2>Requirements</h2>
- Java 8 or higher

<h2>FAQ</h2>
<h3>It crashed</h3>
Just restart it. It will continue where it crashed.

<h3>Why does it need my cookies?</h3>
It needs your cookies to request your <a href=https://steamcommunity.com/my/inventoryhistory>inventory history</a>

<h3>How do I get my cookies?</h3>

<h4>Chrome</h4>
1. Visit your steam profile.
2. Press F12 or right click(anywhere on the website) and press inspect element. 
3. Select the network tab
4. Reload the page
5. Go back to the network tab and scroll to the top and select the first entry. On the right side it should say Request Url: Your steam profile url
6. Scroll down on the right side till you see Cookie:
7. Right click on Cookie: and select copy value

<h4>Firefox</h4>
1. Visit your steam profile.
2. Press F12 or right click(anywhere on the website) and press inspect element.
3. Select the network tab
4. Reload the page
5. Go back to the network tab and scroll to the top and select the first entry. On the right side it should say GET: Your steam profile url
6. Scroll down on the right side till you see Cookie:
7. Right click on Cookie: and select copy
