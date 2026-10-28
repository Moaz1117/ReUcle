const http=require('http');
http.createServer((q,r)=>{r.setHeader('Content-Type','application/json');r.end(JSON.stringify({service:'material-service',status:'UP'}))}).listen(3005,()=>console.log('material-service on 3005'));
