const http=require('http');
http.createServer((q,r)=>{r.setHeader('Content-Type','application/json');r.end(JSON.stringify({service:'payment-service',status:'UP'}))}).listen(3008,()=>console.log('payment-service on 3008'));
