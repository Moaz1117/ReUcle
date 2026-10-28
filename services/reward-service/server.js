const http=require('http');
http.createServer((q,r)=>{r.setHeader('Content-Type','application/json');r.end(JSON.stringify({service:'reward-service',status:'UP'}))}).listen(3007,()=>console.log('reward-service on 3007'));
