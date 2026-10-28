const http=require('http');
http.createServer((q,r)=>{r.setHeader('Content-Type','application/json');r.end(JSON.stringify({service:'marketplace-service',status:'UP'}))}).listen(3010,()=>console.log('marketplace-service on 3010'));
