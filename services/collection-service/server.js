const http=require('http');
http.createServer((q,r)=>{r.setHeader('Content-Type','application/json');r.end(JSON.stringify({service:'collection-service',status:'UP'}))}).listen(3003,()=>console.log('collection-service on 3003'));
