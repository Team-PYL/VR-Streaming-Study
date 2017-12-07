import matplotlib.pyplot as plt
from collections import OrderedDict


def drawingGraph(data, video_name):
    print("drawing!1")
    ord_data_by_timestamp = OrderedDict()
    data_by_timestamp = data[video_name]
    arr_keys = list(data_by_timestamp.keys())
    arr_keys.sort(key=int, reverse=False)
    print("drawing!2")

    for key in arr_keys:
        try:
            key = str(key)
            print(key)
            print(data_by_timestamp[key]['yaw'])
            print(data_by_timestamp[key]['pitch'])
            print(data_by_timestamp[key]['roll'])
            ord_data_by_timestamp[key] = float(data_by_timestamp[key]['yaw']) + float(
                data_by_timestamp[key]['pitch']) + float(data_by_timestamp[key]['roll'])
        except:
            pass



    print("drawing!3")
    arr_keys_int = []
    arr_data_int = []
    for key, value in ord_data_by_timestamp.items():
        arr_keys_int.append(int(key))
        arr_data_int.append(int(value))
    print("keys:", arr_keys_int)
    print("data:", arr_data_int)
    plt.plot(arr_keys_int, arr_data_int, 'ro-')
    plt.xlabel('Timestamp')
    plt.ylabel('FoV data')
    plt.axis([arr_keys_int[0], arr_keys_int[-1], min(arr_data_int), max(arr_data_int)])
    plt.grid(True)
    plt.show()

