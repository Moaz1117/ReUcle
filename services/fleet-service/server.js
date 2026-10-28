const http=require('http');
http.createServer((q,r)=>{r.setHeader('Content-Type','application/json');r.end(JSON.stringify({service:'fleet-service',status:'UP'}))}).listen(3004,()=>console.log('fleet-service on 3004'));
