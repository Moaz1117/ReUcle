const http=require('http');
http.createServer((q,r)=>{r.setHeader('Content-Type','application/json');r.end(JSON.stringify({service:'user-service',status:'UP'}))}).listen(3001,()=>console.log('user-service on 3001'));
