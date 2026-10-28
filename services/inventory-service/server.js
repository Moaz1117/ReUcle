const http=require('http');
http.createServer((q,r)=>{r.setHeader('Content-Type','application/json');r.end(JSON.stringify({service:'inventory-service',status:'UP'}))}).listen(3006,()=>console.log('inventory-service on 3006'));
