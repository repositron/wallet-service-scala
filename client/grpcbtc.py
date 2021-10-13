import json
import subprocess
from concurrent.futures import ThreadPoolExecutor

#{"datetime":"2019-10-05T14:48:01+01:00",  "amount":1.3}' -plaintext 127.0.0.1:8101 wallet.WalletService.AddBtc
input = {"datetime":"2019-10-06T14:48:01+01:00",  "amount":1.3}
def gRpcFn(input):
    for i in range(100):
        result = subprocess.check_output(
            ['grpcurl', '-plaintext', '-d', 
            json.dumps(input), 
            'localhost:8101', 'wallet.WalletService.AddBtc'])
        dict_result = json.loads(result)
#     print(dict_result)


if __name__ == "__main__":
    with ThreadPoolExecutor(max_workers=4) as e:
        e.submit(gRpcFn, {"datetime":"2019-10-06T14:48:01+01:00",  "amount":20})
        e.submit(gRpcFn, {"datetime":"2019-10-07T14:48:01+01:00",  "amount":2})
        e.submit(gRpcFn, {"datetime":"2019-10-08T14:48:01+01:00",  "amount":3})
        e.submit(gRpcFn, {"datetime":"2019-10-09T14:48:01+01:00",  "amount":4})

