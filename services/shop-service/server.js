const http=require('http');
http.createServer((q,r)=>{r.setHeader('Content-Type','application/json');r.end(JSON.stringify({service:'shop-service',status:'UP'}))}).listen(3009,()=>console.log('shop-service on 3009'));
