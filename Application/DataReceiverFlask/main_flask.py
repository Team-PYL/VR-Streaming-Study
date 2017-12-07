from flask import Flask, request, jsonify
from threading import Thread
import graphDrawer

app = Flask(__name__)

mean_data = {}

@app.route('/data/create', methods=['POST'])
def createJSONData():
    content = request.json
    print(content)
    arr_keys = list(content.keys())
    arr_keys.sort(key=int, reverse=False)

    for key in arr_keys:
        print(key + ": " + str(content[key]))
    t1 = Thread(target=getMeanOfDataPerTimeSlice, args=(content, arr_keys, "congo_2048"))
    t2 = Thread(target=graphDrawer.drawingGraph, args=(mean_data, "congo_2048"))
    t1.start()
    t2.start()
    # getMeanOfDataPerTimeSlice(content, arr_keys, "congo_2048")
    # graphDrawer.drawingGraph(mean_data, "congo_2048")
    return jsonify({"status": "success"})

def getMeanOfDataPerTimeSlice(json_content, json_arrKeys, video_name):
    idx = 0
    idx_range = 1000
    yaw_sum = 0
    roll_sum = 0
    pitch_sum = 0
    cnt = 1

    mean_data[video_name] = dict()
    temp_dict = dict()

    temp_dict[str(idx + idx_range)] = {'yaw': 0.0, 'roll': 0.0, 'pitch': 0.0}

    for time in json_arrKeys:

        if idx <= int(time) <= idx+idx_range:
            index_time = str(time)

            yaw_sum += float(json_content[index_time]['yaw'])
            pitch_sum += float(json_content[index_time]['pitch'])
            roll_sum += float(json_content[index_time]['roll'])
            cnt += 1
        else:
            index_time = str(idx + idx_range)

            temp_dict[index_time] = {}
            print(yaw_sum, pitch_sum, roll_sum, cnt)
            if cnt is not 0:
                temp_dict[index_time]['yaw'] = float(yaw_sum/cnt)
                temp_dict[index_time]['pitch'] = float(pitch_sum / cnt)
                temp_dict[index_time]['roll'] = float(roll_sum / cnt)

            idx += idx_range
            temp_dict[str(idx + idx_range)] = 0
            yaw_sum = 0
            roll_sum = 0
            pitch_sum = 0
            cnt = 0

    mean_data[video_name] = temp_dict

    # print("yaw mean: %f" % mean_data[video_name]['1000']['yaw'])
    # print("pitch mean:", mean_data[video_name][str(idx + idx_range)]['pitch'])
    # print("roll mean:", mean_data[video_name][str(idx + idx_range)]['roll'])


if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)